package com.trevari.spring.trsearchservice.interfaces.mapper;

import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import com.trevari.spring.trsearchservice.infrastructure.persistence.SearchKeywordEntity;
import org.springframework.stereotype.Component;

@Component
public class SearchKeywordMapper {
    public SearchKeywordEntity toEntity(SearchKeyword d) {
        return SearchKeywordEntity.builder()
                .keyword(d.getKeyword())
                .cnt(d.getCount())
                .build();
    }

    public SearchKeyword toDomain(SearchKeywordEntity e) {
        return SearchKeyword.builder()
                .keyword(e.getKeyword())
                .count(e.getCnt())
                .build();
    }
}
