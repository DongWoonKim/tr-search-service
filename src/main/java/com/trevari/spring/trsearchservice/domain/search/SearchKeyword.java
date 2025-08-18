package com.trevari.spring.trsearchservice.domain.search;

public record SearchKeyword(
        String keyword,
        long cnt
) {
    public SearchKeyword {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("keyword must not be blank");
        }
        if (cnt < 0) {
            throw new IllegalArgumentException("cnt must be >= 0");
        }
    }
}
