package com.trevari.spring.trsearchservice.application;

import com.trevari.spring.trsearchservice.domain.search.ParsedQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QueryParser {
    private static final String OR_DELIM = "\\|";
    private static final String SPACE_DELIM = "\\s+";

    private final int maxKeywords;

    /**
     * 기본 정책: 키워드 최대 2개
     */
    public QueryParser() {
        this(2);
    }

    /**
     * @param maxKeywords 키워드 총합(must + OR 평탄화 + mustNot)의 최대 허용 개수
     */
    public QueryParser(int maxKeywords) {
        if (maxKeywords < 1) throw new IllegalArgumentException("maxKeywords must be >= 1");
        this.maxKeywords = maxKeywords;
    }

    /**
     * 주어진 raw 문자열을 파싱해 ParsedQuery를 생성한다.
     * null/blank 입력은 모두 빈 쿼리로 간주한다.
     */
    public ParsedQuery parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return emptyQuery();
        }

        // 정규화: trim + lower
        String normalized = normalize(raw);

        // 누적용(입력 순서 유지)
        LinkedHashSet<String> must = new LinkedHashSet<>();
        LinkedHashSet<String> mustNot = new LinkedHashSet<>();
        List<Set<String>> shouldGroups = new ArrayList<>();

        for (String token : normalized.split(SPACE_DELIM)) {
            if (token.isBlank()) continue;

            if (isNotToken(token)) {
                // NOT: 첫 글자 '-' 제거 후 정제
                addIfPresent(mustNot, token.substring(1));
            } else if (isOrGroup(token)) {
                // OR 그룹: '|'로 나눠 정제/중복제거 후 그룹으로 저장
                Set<String> group = splitOrGroup(token);
                if (!group.isEmpty()) {
                    shouldGroups.add(group);
                }
            } else {
                // 일반 토큰
                addIfPresent(must, token);
            }
        }

        // 키워드 개수 제한 적용(정책)
        enforceMaxKeywords(must, shouldGroups, mustNot, maxKeywords);

        // 외부 변경 불가하도록 불변 래핑 후 VO 생성
        return new ParsedQuery(
                Collections.unmodifiableSet(must),
                shouldGroups.stream()
                        .map(set -> Collections.unmodifiableSet(new LinkedHashSet<>(set)))
                        .collect(Collectors.toUnmodifiableList()),
                Collections.unmodifiableSet(mustNot)
        );
    }

    /* =========================
       내부 유틸/헬퍼
       ========================= */

    private static String normalize(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isNotToken(String token) {
        return token.startsWith("-") && token.length() > 1;
    }

    private static boolean isOrGroup(String token) {
        return token.contains("|");
    }

    private static void addIfPresent(Set<String> target, String token) {
        String t = token.trim();
        // 단독 하이픈(예: "-")은 무시
        if (t.isBlank() || "-".equals(t)) {
            return;
        }
        target.add(t);
    }

    /**
     * OR 그룹을 분해해 공백/빈문자 제거, 중복 제거(입력 순서 유지).
     * 예) "tdd|javascript" -> ["tdd","javascript"]
     */
    private static Set<String> splitOrGroup(String token) {
        return Arrays.stream(token.split(OR_DELIM))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static ParsedQuery emptyQuery() {
        return new ParsedQuery(Set.of(), List.of(), Set.of());
    }

    /**
     * must + OR 그룹 평탄화 + mustNot의 총합이 maxKeywords를 초과하면
     * 초과분을 잘라낸다(정책). 필요 시 예외로 바꿀 수 있음.
     */
    private static void enforceMaxKeywords(Set<String> must,
                                           List<Set<String>> shouldGroups,
                                           Set<String> mustNot,
                                           int maxKeywords) {
        int remaining = maxKeywords;

        // 1) MUST에서 remaining만큼만 남김
        trimSetInPlace(must, remaining);
        remaining -= must.size();
        if (remaining <= 0) {
            shouldGroups.clear();
            mustNot.clear();
            return;
        }

        // 2) SHOULD 그룹(평탄화 기준)에서 remaining만큼만 남김
        int keptFromShould = trimShouldGroupsInPlace(shouldGroups, remaining);
        remaining -= keptFromShould;
        if (remaining <= 0) {
            mustNot.clear();
            return;
        }

        // 3) MUST_NOT에서 remaining만큼만 남김
        trimSetInPlace(mustNot, remaining);
    }

    /**
     * Set을 앞에서부터 remaining 개수만 남기고 잘라낸다.
     */
    private static void trimSetInPlace(Set<String> set, int remaining) {
        if (remaining <= 0) {
            set.clear();
            return;
        }
        if (set.size() <= remaining) return;

        Iterator<String> it = set.iterator();
        int kept = 0;
        LinkedHashSet<String> trimmed = new LinkedHashSet<>();
        while (it.hasNext() && kept < remaining) {
            trimmed.add(it.next());
            kept++;
        }
        set.clear();
        set.addAll(trimmed);
    }

    /**
     * OR 그룹 전체를 평탄화 기준으로 remaining만큼만 남기고 이후는 그룹에서 제거.
     * @return 실제 남은 total count (OR 평탄화 기준)
     */
    private static int trimShouldGroupsInPlace(List<Set<String>> groups, int remaining) {
        if (remaining <= 0) {
            groups.clear();
            return 0;
        }
        int keptTotal = 0;
        ListIterator<Set<String>> it = groups.listIterator();
        while (it.hasNext()) {
            Set<String> group = it.next();
            if (group.isEmpty()) {
                it.remove();
                continue;
            }
            if (keptTotal >= remaining) {
                it.remove();
                continue;
            }
            int canKeep = remaining - keptTotal;
            if (group.size() > canKeep) {
                // 그룹에서 앞의 canKeep만 유지
                LinkedHashSet<String> trimmed = new LinkedHashSet<>();
                int i = 0;
                for (String s : group) {
                    if (i++ >= canKeep) break;
                    trimmed.add(s);
                }
                it.set(trimmed);
                keptTotal += trimmed.size();
            } else {
                keptTotal += group.size();
            }
        }
        return keptTotal;
    }
}
