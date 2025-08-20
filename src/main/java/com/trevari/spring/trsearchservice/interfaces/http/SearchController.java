package com.trevari.spring.trsearchservice.interfaces.http;

import com.trevari.spring.trsearchservice.application.SearchService;
import com.trevari.spring.trsearchservice.interfaces.dto.SearchResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Search", description = "도서 검색 API (OR/NOT 연산자 지원)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @Operation(
            summary = "도서 검색",
            description = """
            키워드 기반 도서 검색을 수행합니다.
            - 공백으로 AND 검색
            - OR(|) 연산자: `java%7Ckotlin` 처럼 URL 인코딩 필요
            - NOT(-) 연산자: `-python`
            예) `q=java tdd%7C-python`
            """
    )
    @GetMapping
    public SearchResultDto search(@RequestParam String q,
                                  @RequestParam(defaultValue="0") int page,
                                  @RequestParam(defaultValue="10") int size) {
        return searchService.search(q, page, size);
    }

    @Operation(
            summary = "인기 검색어 TOP10",
            description = "최근 집계된 인기 검색어 상위 10개를 반환합니다."
    )
    @GetMapping("/popular")
    public List<String> popular() {
        return searchService.popularTop10();
    }

}
