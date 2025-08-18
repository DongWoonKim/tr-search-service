package com.trevari.spring.trsearchservice.infrastructure.persistence;

import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import com.trevari.spring.trsearchservice.domain.search.SearchKeywordRepository;
import com.trevari.spring.trsearchservice.interfaces.mapper.SearchKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchKeywordRepositoryAdapter implements SearchKeywordRepository {
    private final SearchKeywordJpaRepository jpa;
    private final SearchKeywordMapper mapper;

    @Override
    public SearchKeyword save(SearchKeyword keyword) {
        var saved = jpa.save(mapper.toEntity(keyword));
        return mapper.toDomain(saved);
    }
}
