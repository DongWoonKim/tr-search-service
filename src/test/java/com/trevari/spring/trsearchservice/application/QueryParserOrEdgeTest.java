package com.trevari.spring.trsearchservice.application;

import com.trevari.spring.trsearchservice.domain.search.ParsedQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryParserOrEdgeTest {

    private final QueryParser parser = new QueryParser(5);

    @Test
    void 빈_OR_토큰_무시_테스트() {
        // given
        ParsedQuery q = parser.parse("||tdd||");

        // when
        // 공백/빈문자 제거되므로 "tdd" 만 남음
        int total = q.must().size()
                + (int) q.shouldGroups().stream().flatMap(java.util.Collection::stream).count()
                + q.mustNot().size();

        // then
        assertThat(total).isEqualTo(1);
        assertThat(q.shouldGroups()).hasSize(1);
        assertThat(q.shouldGroups().get(0)).containsExactly("tdd");
    }

    @Test
    void OR_여러_그룹_테스트() {
        // given, when
        ParsedQuery q = parser.parse("a|b c|d");

        // then
        assertThat(q.shouldGroups()).hasSize(2);
        assertThat(q.shouldGroups().get(0)).containsExactly("a", "b");
        assertThat(q.shouldGroups().get(1)).containsExactly("c", "d");
    }
}
