package com.trevari.spring.trsearchservice.interfaces.mapper;

import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import com.trevari.spring.trsearchservice.domain.search.SearchKeywordRepository;

public class SearchKeywordRepositoryAdapter implements SearchKeywordRepository {
    @Override
    public SearchKeyword save(SearchKeyword keyword) {
        return null;
    }
}
