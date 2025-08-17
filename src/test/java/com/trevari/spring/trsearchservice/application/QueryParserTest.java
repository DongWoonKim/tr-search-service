package com.trevari.spring.trsearchservice.application;

import com.trevari.spring.trsearchservice.domain.search.ParsedQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryParserTest {

    private final QueryParser parser = new QueryParser(); // max 2 keywords 정책

    @Test
    void 빈_입력은_빈_쿼리_테스트() {
        // given, when
        ParsedQuery q1 = parser.parse(null);
        ParsedQuery q2 = parser.parse("   ");

        // then
        assertThat(q1.must()).isEmpty();
        assertThat(q1.mustNot()).isEmpty();
        assertThat(q1.shouldGroups()).isEmpty();

        assertThat(q2.must()).isEmpty();
        assertThat(q2.mustNot()).isEmpty();
        assertThat(q2.shouldGroups()).isEmpty();
    }

    @Test
    void 일반_키워드_must_테스트() {
        // given, when
        ParsedQuery q = parser.parse("tdd");

        // then
        assertThat(q.must()).containsExactly("tdd");
        assertThat(q.mustNot()).isEmpty();
        assertThat(q.shouldGroups()).isEmpty();
    }

    @Test
    void OR_연산자_shouldGroups_한_그룹() {
        // given, when
        ParsedQuery q = parser.parse("tdd|javascript");

        // then
        assertThat(q.must()).isEmpty();
        assertThat(q.shouldGroups()).hasSize(1);
        assertThat(q.shouldGroups().get(0)).containsExactly("tdd", "javascript");
        assertThat(q.mustNot()).isEmpty();
    }

    @Test
    void NOT_연산자_mustNot() {
        // given, when
        ParsedQuery q = parser.parse("-java");

        // then
        assertThat(q.must()).isEmpty();
        assertThat(q.mustNot()).containsExactly("java");
        assertThat(q.shouldGroups()).isEmpty();
    }

    @Test
    void 혼합_케이스_must_OR_mustNot() {
        // given, when
        ParsedQuery q = parser.parse("spring tdd|clean -legacy");

        // then
        // 기본 정책(max=2)이 적용되므로 총 2개만 남음 (앞에서부터 보존)
        // 입력 순서 기준: must("spring") 1개 + OR("tdd","clean")에서 1개만 남고, mustNot는 잘림
        assertThat(q.must()).containsExactly("spring");
        assertThat(q.shouldGroups()).hasSize(1);
        assertThat(q.shouldGroups().get(0)).hasSize(1);
        assertThat(q.mustNot()).isEmpty();
    }

    @Test
    void 대소문자_정규화() {
        // given, when
        ParsedQuery q = parser.parse("   TDD   ");

        // then
        assertThat(q.must()).containsExactly("tdd");
    }

    @Test
    void 공백과_중복_제거() {
        // given, when
        ParsedQuery q = parser.parse("tdd   tdd  |  tdd  ");

        // then
        // max=2 → 결국 총 2개만 유지, 중복 제거
        // must: ["tdd"], shouldGroups: [["tdd"]] (또는 must에서만 1개일 수도)
        assertThat(q.must()).contains("tdd");
        assertThat(q.must()).hasSizeLessThanOrEqualTo(1);
        assertThat(q.shouldGroups()).allSatisfy(g -> assertThat(g).hasSizeLessThanOrEqualTo(1));
    }
}