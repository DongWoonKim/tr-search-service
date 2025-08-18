package com.trevari.spring.trsearchservice.domain.search;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchKeyword {
    private final String keyword;
    private final long count;
}
