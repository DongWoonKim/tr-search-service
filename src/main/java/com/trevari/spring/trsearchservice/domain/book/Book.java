package com.trevari.spring.trsearchservice.domain.book;

public record Book(
        String isbn,
        String title,
        String subtitle,
        String author,
        String publisher,
        String publishedDate
) {
}
