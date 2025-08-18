package com.trevari.spring.trsearchservice.application;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.util.ObjectBuilder;
import com.trevari.spring.trsearchservice.domain.search.ParsedQuery;
import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import com.trevari.spring.trsearchservice.domain.search.SearchKeywordRepository;
import com.trevari.spring.trsearchservice.exception.SearchException;
import com.trevari.spring.trsearchservice.infrastructure.persistence.BookDoc;
import com.trevari.spring.trsearchservice.interfaces.dto.BookSummaryDto;
import com.trevari.spring.trsearchservice.interfaces.dto.SearchResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock
    ElasticsearchClient es;

    @Mock
    SearchKeywordRepository keywordRepo;

    @Mock
    QueryParser queryParser;

    @InjectMocks
    SearchService service;

    private BookDoc doc1;
    private BookDoc doc2;

    @BeforeEach
    void setUp() {
        doc1 = new BookDoc("1", "9780134685991", "Effective Java", "Best", "Joshua Bloch",
                "Addison-Wesley", LocalDate.parse("2018-01-06"));
        doc2 = new BookDoc("2", "9780132350884", "Clean Code", "A Handbook", "Robert C. Martin",
                "Prentice Hall", LocalDate.parse("2008-08-01"));
    }

    @Test
    void search_성공시_아이템매핑_및_인기검색어증가_테스트() throws Exception {
        // given
        String raw = " Java  tdd|-python  "; // 앞뒤 공백 포함 → trim 기대
        ParsedQuery pq = new ParsedQuery(
                new LinkedHashSet<>(List.of("java")),              // must
                List.of(new LinkedHashSet<>(List.of("tdd"))),      // shouldGroups
                new LinkedHashSet<>(List.of("python"))             // mustNot
        );
        when(queryParser.parse(raw)).thenReturn(pq);

        SearchResponse<BookDoc> esResp = buildEsResponse(List.of(doc1, doc2), 2L);

        // ElasticsearchClient#search(Function<Builder,ObjectBuilder<SearchRequest>>, Class<BookDoc>)
        when(es.search(
                ArgumentMatchers.<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>>any(),
                eq(BookDoc.class)
        )).thenReturn(esResp);

        // when
        SearchResultDto result = service.search(raw, 0, 10);

        // then
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.items())
                .extracting(BookSummaryDto::isbn)
                .containsExactlyInAnyOrder("9780134685991", "9780132350884");
        verify(keywordRepo).increaseCount("Java  tdd|-python".trim());
    }

    @Test
    void search_ES예외시_SearchException_전파_테스트() throws Exception {
        // given
        String raw = "java";
        ParsedQuery pq = new ParsedQuery(
                Set.of("java"),
                List.of(),
                Set.of()
        );
        when(queryParser.parse(raw)).thenReturn(pq);

        @SuppressWarnings("unchecked")
        Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> anyFn =
                (Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>) any(Function.class);
        when(es.search(anyFn, eq(BookDoc.class))).thenThrow(new RuntimeException("es down"));

        // when / then
        assertThatThrownBy(() -> service.search(raw, 0, 10))
                .isInstanceOf(SearchException.class)
                .hasMessageContaining("Search failed");
        // 인기검색어는 증가 안 했는지(실패 시 호출 X)
        verify(keywordRepo, never()).increaseCount(anyString());
    }

    @Test
    void search_page_size_검증_테스트() {
        // page < 0
        assertThatThrownBy(() -> service.search("java", -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page must be >= 0");

        // size <= 0
        assertThatThrownBy(() -> service.search("java", 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size must be > 0");

        // size > 100
        assertThatThrownBy(() -> service.search("java", 0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size must be <= 100");
    }

    @Test
    void search_page_size_곱셈_오버플로우_검출_테스트() {
        // page * size 가 int 범위를 넘는 케이스 유도
        int page = Integer.MAX_VALUE;
        int size = 2;
        assertThatThrownBy(() -> service.search("java", page, size))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overflows int range");
    }

    @Test
    void popularTop10_키워드목록_반환_테스트() {
        // given
        when(keywordRepo.top10()).thenReturn(List.of(
                new SearchKeyword("java", 10),
                new SearchKeyword("spring", 8),
                new SearchKeyword("elasticsearch", 5)
        ));

        // when
        List<String> top = service.popularTop10();

        // then
        assertThat(top).containsExactly("java", "spring", "elasticsearch");
    }


    private static SearchResponse<BookDoc> buildEsResponse(List<BookDoc> docs, long total) {
        List<Hit<BookDoc>> hits = docs.stream()
                .map(d -> Hit.<BookDoc>of(h -> h
                        .index("books")
                        .id(d.isbn())          // 테스트용으로 isbn을 id로 사용
                        .source(d)
                ))
                .toList();

        return SearchResponse.of(r -> r
                .took(5)                       // ✅ 필수
                .timedOut(false)               // ✅ 필수
                .shards(s -> s                 // ✅ 필수
                        .total(1)
                        .successful(1)
                        .skipped(0)
                        .failed(0)
                )
                .hits(h -> h
                        .total(t -> t.value(total).relation(TotalHitsRelation.Eq))
                        .hits(hits)
                )
        );
    }
}