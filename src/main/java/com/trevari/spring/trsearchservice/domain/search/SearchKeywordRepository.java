package com.trevari.spring.trsearchservice.domain.search;

import java.util.List;

public interface SearchKeywordRepository {
    SearchKeyword save(SearchKeyword keyword);
    List<SearchKeyword> top10();
    /** 키워드 집계: 있으면 cnt++, 없으면 새로 insert */
    void increaseCount(String keyword);
}
