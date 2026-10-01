package com.banlinhkien.repository;

import com.banlinhkien.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    List<NewsArticle> findByIsPublishedTrueOrderByPublishedAtDesc();

    Page<NewsArticle> findByIsPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

    Optional<NewsArticle> findBySlug(String slug);

    Page<NewsArticle> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String title, String summary, Pageable pageable);
}
