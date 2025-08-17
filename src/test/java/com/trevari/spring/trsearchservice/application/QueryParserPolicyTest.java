package com.trevari.spring.trsearchservice.application;

import com.trevari.spring.trsearchservice.domain.search.ParsedQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class QueryParserPolicyTest {

    @Test
    void 최대_키워드_2개_정책_적용_테스트() {
        // given
        QueryParser parser = new QueryParser(2);
        ParsedQuery q = parser.parse("tdd|java spring -legacy");

        // when
        // 평탄화 기준 총 입력 키워드: "tdd","java","spring","legacy" (4개)
        // 2개만 남겨야 함(앞에서부터 남기기): OR 그룹에서 1개만 남고, mustNot은 잘릴 수 있음
        int total =
                q.must().size()
                        + (int) q.shouldGroups().stream().flatMap(java.util.Collection::stream).count()
                        + q.mustNot().size();

        // then
        assertThat(total).isLessThanOrEqualTo(2);
    }

    @Test
    void 최대_키워드_3개로_완화하면_OR_한_그룹_2개까지_허용될_수_있음_테스트() {
        // given, when
        QueryParser parser = new QueryParser(3);
        ParsedQuery q = parser.parse("spring tdd|java -legacy");

        // then
        // 남는 전략: must("spring") 1 + OR("tdd","java") 2 = 3 → mustNot은 잘림
        assertThat(q.must()).containsExactly("spring");
        assertThat(q.shouldGroups()).hasSize(1);
        assertThat(q.shouldGroups().get(0)).containsExactly("tdd", "java");
        assertThat(q.mustNot()).isEmpty();
    }
}
