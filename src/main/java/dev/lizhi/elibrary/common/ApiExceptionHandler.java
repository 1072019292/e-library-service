package dev.lizhi.elibrary.common;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private final Problems problems;

    public ApiExceptionHandler(Problems problems) { this.problems = problems; }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> business(ApiException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.status()).body(problems.create(exception.status(),
                exception.code(), exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> validation(ConstraintViolationException exception, HttpServletRequest request) {
        ProblemDetail problem = problems.create(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Request validation failed.", request.getRequestURI());
        problem.setProperty("errors", exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage()).sorted().toList());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler({PessimisticLockingFailureException.class, QueryTimeoutException.class,
            PessimisticLockException.class, LockTimeoutException.class})
    ResponseEntity<ProblemDetail> lockTimeout(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header(HttpHeaders.RETRY_AFTER, "1")
                .body(problems.create(HttpStatus.SERVICE_UNAVAILABLE, "BOOK_BUSY",
                        "The book is busy. Retry the request shortly.", request.getRequestURI()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String code = switch (status.value()) {
            case 400 -> "INVALID_REQUEST";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> status.is5xxServerError() ? "INTERNAL_ERROR" : "REQUEST_REJECTED";
        };
        String detail = switch (status.value()) {
            case 400 -> "Request parameters or JSON body are invalid.";
            case 404 -> "The requested resource was not found.";
            case 405 -> "This HTTP method is not supported for this resource.";
            case 406 -> "The requested response format is not supported.";
            case 415 -> "Use Content-Type: application/json for borrowing requests.";
            default -> status.is5xxServerError() ? "An unexpected error occurred." : "The request was rejected.";
        };
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ProblemDetail problem = problems.create(status, code, detail, path);
        if (exception instanceof MethodArgumentNotValidException invalid) {
            problem.setProperty("errors", invalid.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage()).sorted().toList());
        }
        if (status.is5xxServerError()) { log.error("Request failed: {}", path, exception); }
        return new ResponseEntity<>(problem, headers, status);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected request failure: {}", request.getRequestURI(), exception);
        return ResponseEntity.internalServerError().body(problems.create(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR", "An unexpected error occurred.", request.getRequestURI()));
    }
}
