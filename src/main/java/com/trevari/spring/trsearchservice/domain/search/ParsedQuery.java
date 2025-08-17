package com.trevari.spring.trsearchservice.domain.search;

import java.util.List;
import java.util.Set;

public record ParsedQuery(
        Set<String> must,
        List<Set<String>> shouldGroups,
        Set<String> mustNot
) {
}
