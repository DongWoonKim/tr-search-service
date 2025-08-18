package com.trevari.spring.trsearchservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchKeywordJpaRepository extends JpaRepository<SearchKeywordEntity, Long> {
}
