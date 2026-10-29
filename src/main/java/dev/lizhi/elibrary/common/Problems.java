package dev.lizhi.elibrary.common;

import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class Problems {
    public ProblemDetail create(HttpStatusCode status, String code, String detail, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:elibrary:error:" + code.toLowerCase(Locale.ROOT).replace('_', '-')));
        problem.setInstance(URI.create(path));
        problem.setProperty("code", code);
        return problem;
    }
}
