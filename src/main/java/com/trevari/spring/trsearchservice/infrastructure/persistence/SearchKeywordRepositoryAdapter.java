package com.trevari.spring.trsearchservice.infrastructure.persistence;

import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import com.trevari.spring.trsearchservice.domain.search.SearchKeywordRepository;
import com.trevari.spring.trsearchservice.interfaces.mapper.SearchKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<SearchKeyword> top10() {
        return jpa.findTopOrderByCntDesc(PageRequest.of(0, 10))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void increaseCount(String keyword) {
        jpa.findByKeyword(keyword).ifPresentOrElse(
                entity -> {
                    entity.setCnt(entity.getCnt() + 1);
                    jpa.save(entity);
                },
                () -> {
                    var newEntity = SearchKeywordEntity.builder()
                            .keyword(keyword)
                            .cnt(1L)
                            .build();
                    jpa.save(newEntity);
                }
        );
    }
}
