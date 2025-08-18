package com.trevari.spring.trsearchservice.infrastructure.persistence;

import com.trevari.spring.trsearchservice.domain.search.SearchKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordJpaRepository extends JpaRepository<SearchKeywordEntity, Long> {
    @Query("select k from SearchKeywordEntity k order by k.cnt desc")
    List<SearchKeywordEntity> findTopOrderByCntDesc(Pageable pageable);

    Optional<SearchKeywordEntity> findByKeyword(String keyword);
}
