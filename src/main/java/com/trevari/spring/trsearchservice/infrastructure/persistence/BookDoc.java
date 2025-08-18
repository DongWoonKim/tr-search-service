package com.trevari.spring.trsearchservice.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.trevari.spring.trsearchservice.interfaces.dto.BookSummaryDto;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 있어도 모델에 없으면 무시
public record BookDoc(
        String id, String isbn, String title, String subtitle,
        String author, String publisher, LocalDate publishedDate
) {
    public BookSummaryDto toSummaryDto() {
        return BookSummaryDto.builder()
                .isbn(isbn)
                .title(title)
                .subtitle(subtitle)
                .author(author)
                .publisher(publisher)
                .publishedDate(publishedDate)
                .build();
    }
}
