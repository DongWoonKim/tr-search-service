package com.trevari.spring.trsearchservice.interfaces.mapper;

import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import com.trevari.spring.trsearchservice.infrastructure.persistence.SearchKeywordEntity;
import org.springframework.stereotype.Component;

@Component
public class SearchKeywordMapper {
    public SearchKeyword toDomain(SearchKeywordEntity e) {
        return new SearchKeyword(
                e.getKeyword(),
                e.getCnt()
        );
    }

    public SearchKeywordEntity toEntity(SearchKeyword d) {
        return SearchKeywordEntity.builder()
                .keyword(d.keyword())
                .cnt(d.cnt())
                .build();
    }
}
