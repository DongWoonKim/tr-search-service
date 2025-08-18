package com.trevari.spring.trsearchservice.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="search_keyword")
public class SearchKeywordEntity {
    @Id
    @Column(length=200)
    private String keyword;

    @Column(nullable=false)
    private long cnt;
}
