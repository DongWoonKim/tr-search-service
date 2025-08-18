package com.trevari.spring.trsearchservice.interfaces.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record BookSummaryDto(
        String isbn,
        String title,
        String subtitle,
        String author,
        String publisher,
        LocalDate publishedDate
) {}
