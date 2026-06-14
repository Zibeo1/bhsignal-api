package com.geonews.integration.repository;

import com.geonews.integration.domain.NewsItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface NewsItemRepository extends JpaRepository<NewsItemEntity, UUID>, JpaSpecificationExecutor<NewsItemEntity> {

    Optional<NewsItemEntity> findBySourceAndSourceArticleId(String source, String sourceArticleId);
}
