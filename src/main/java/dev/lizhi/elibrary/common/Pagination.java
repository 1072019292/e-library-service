package dev.lizhi.elibrary.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

public final class Pagination {
    private Pagination() { }

    public static PageRequest request(int page, int size) {
        // JPA 的 firstResult 只接受 int；超大页码应作为输入错误，而非执行查询后返回 500。
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Page size or offset is out of range.");
        }
        return PageRequest.of(page, size);
    }
}
