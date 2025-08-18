package com.trevari.spring.trsearchservice.interfaces.http;

import com.trevari.spring.trsearchservice.application.SearchService;
import com.trevari.spring.trsearchservice.interfaces.dto.SearchResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchResultDto search(@RequestParam String q,
                                  @RequestParam(defaultValue="0") int page,
                                  @RequestParam(defaultValue="20") int size) {
        return searchService.search(q, page, size);
    }

    @GetMapping("/popular")
    public List<String> popular() {
        return searchService.popularTop10();
    }

}
