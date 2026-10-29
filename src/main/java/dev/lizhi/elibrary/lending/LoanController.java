package dev.lizhi.elibrary.lending;

import dev.lizhi.elibrary.common.PageResponse;
import dev.lizhi.elibrary.identity.LibraryPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class LoanController {
    private final LendingService lending;

    public LoanController(LendingService lending) { this.lending = lending; }

    @PostMapping(value = "/api/loans", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LoanView borrow(@AuthenticationPrincipal LibraryPrincipal reader, @Valid @RequestBody BorrowRequest request) {
        return lending.borrow(reader.id(), request.bookId());
    }

    @PutMapping("/api/loans/{loanId}/return")
    public LoanView returnLoan(@AuthenticationPrincipal LibraryPrincipal reader, @PathVariable @Positive long loanId) {
        return lending.returnLoan(reader.id(), loanId);
    }

    @GetMapping("/api/me/loans")
    public PageResponse<LoanView> current(@AuthenticationPrincipal LibraryPrincipal reader,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return lending.current(reader.id(), page, size);
    }
}
