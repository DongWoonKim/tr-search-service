package com.trevari.spring.trsearchservice.application;

import com.trevari.spring.trsearchservice.domain.search.ParsedQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryParserNotEdgeTest {
    private final QueryParser parser = new QueryParser(2);

    @Test
    void 하이픈만_있는_토큰은_무시_테스트() {
        // given, when
        ParsedQuery q = parser.parse("-  -   -  ");

        // then
        assertThat(q.mustNot()).isEmpty();
        assertThat(q.must()).isEmpty();
        assertThat(q.shouldGroups()).isEmpty();
    }

    @Test
    void 단독_하이픈은_무시되고_정상_토큰만_남는다_테스트() {
        // given, when
        ParsedQuery q = parser.parse("-  java  -  tdd  -");

        // then
        assertThat(q.must()).containsExactly("java", "tdd");
        assertThat(q.mustNot()).isEmpty();
        assertThat(q.shouldGroups()).isEmpty();
    }

    @Test
    void NOT_여러개_테스트() {
        // given, when
        ParsedQuery q = parser.parse("-java -python -go");

        // then
        // case1) maxKeywords=2가 "must만 제한"이면 그대로 유지
        // case2) maxKeywords=2가 "전체 합산 제한"이면 go는 무시됨
        assertThat(q.mustNot()).containsExactly("java", "python");
    }
}
