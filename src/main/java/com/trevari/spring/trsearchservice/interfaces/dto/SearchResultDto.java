package com.trevari.spring.trsearchservice.interfaces.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SearchResultDto(
        List<BookSummaryDto> items,
        long total,
        int page,
        int size,
        String query
) {}