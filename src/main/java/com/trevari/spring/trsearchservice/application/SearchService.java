package com.trevari.spring.trsearchservice.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.ObjectBuilder;
import com.trevari.spring.trsearchservice.domain.search.ParsedQuery;
import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import com.trevari.spring.trsearchservice.domain.search.SearchKeywordRepository;
import com.trevari.spring.trsearchservice.exception.SearchException;
import com.trevari.spring.trsearchservice.infrastructure.persistence.BookDoc;
import com.trevari.spring.trsearchservice.interfaces.dto.SearchResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SearchService {
    private static final String INDEX_BOOKS = "books";
    private static final List<String> SEARCH_FIELDS =
            List.of("title^3", "subtitle^2", "author", "publisher", "isbn");

    private final ElasticsearchClient es;
    private final SearchKeywordRepository keywordRepo;
    private final QueryParser queryParser;

    public SearchResultDto search(String rawQuery, int page, int size) {
        validatePageSize(page, size);
        final ParsedQuery parsed = queryParser.parse(rawQuery);
        final int from = safeFrom(page, size);

        try {
            SearchResponse<BookDoc> resp = es.search(s -> {
                s.index(INDEX_BOOKS)
                        .from(from)
                        .size(size)
                        .sort(so -> so.field(f -> f.field("publishedDate").order(SortOrder.Desc)))
                        .query(qb -> qb.bool(buildBool(parsed)));
                return s;
            }, BookDoc.class);

            if (rawQuery != null && !rawQuery.isBlank()) {
                keywordRepo.increaseCount(rawQuery.trim());
            }

            var items = mapHits(resp, BookDoc::toSummaryDto);
            long total = resp.hits().total() == null ? items.size() : resp.hits().total().value();

            return SearchResultDto.builder()
                    .items(items)
                    .total(total)
                    .page(page)
                    .size(size)
                    .query(rawQuery)
                    .build();

        } catch (Exception e) {
            throw new SearchException("Search failed", e);
        }
    }

    public List<String> popularTop10() {
        return keywordRepo.top10().stream()
                .map(SearchKeyword::keyword)
                .toList();
    }

    private static void validatePageSize(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        if (size > 100) throw new IllegalArgumentException("size must be <= 100");
    }

    private static int safeFrom(int page, int size) {
        try {
            return Math.toIntExact(Math.multiplyExact((long) page, (long) size));
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("page*size overflows int range", ex);
        }
    }

    private static Function<BoolQuery.Builder, ObjectBuilder<BoolQuery>> buildBool(ParsedQuery pq) {
        return b -> {
            for (String term : pq.must()) {
                b.must(m -> m.multiMatch(mm -> multiMatch(term)));
            }
            for (Set<String> group : pq.shouldGroups()) {
                b.should(sh -> sh.bool(inner -> {
                    group.forEach(term -> inner.should(sh2 -> sh2.multiMatch(mm -> multiMatch(term))));
                    return inner.minimumShouldMatch("1");
                }));
            }
            for (String term : pq.mustNot()) {
                b.mustNot(mn -> mn.multiMatch(mm -> multiMatch(term)
                        .fields(List.of("title","subtitle","author","publisher","isbn"))));
            }
            return b;
        };
    }

    private static MultiMatchQuery.Builder multiMatch(String term) {
        return new MultiMatchQuery.Builder()
                .query(term)
                .fields(SEARCH_FIELDS);
    }

    private static <T> List<T> mapHits(SearchResponse<? extends BookDoc> resp,
                                       Function<BookDoc, T> mapper) {
        return resp.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(mapper)
                .toList();
    }

}
