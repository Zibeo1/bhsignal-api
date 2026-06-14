package com.geonews.integration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "news_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_news_source_article", columnNames = {"source", "source_article_id"})
        },
        indexes = {
                @Index(name = "idx_news_published_at", columnList = "published_at"),
                @Index(name = "idx_news_category", columnList = "category"),
                @Index(name = "idx_news_location", columnList = "location_name")
        }
)
public class NewsItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 32)
    private String source;

    @Column(name = "source_article_id", nullable = false, length = 64)
    private String sourceArticleId;

    @Column(nullable = false, length = 700)
    private String title;

    @Column(name = "summary_text", columnDefinition = "text", nullable = false)
    private String summary;

    @Column(nullable = false, length = 800)
    private String url;

    @Column(length = 128)
    private String category;

    @Column(length = 128)
    private String author;

    @Column(length = 900)
    private String imageUrl;

    @Column(nullable = false)
    private Instant publishedAt;

    @Column(length = 128)
    private String locationTagRaw;

    @Column(length = 128)
    private String locationName;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private double locationConfidence;

    @Column(nullable = false, length = 32)
    private String precision;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Instant ingestedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceArticleId() {
        return sourceArticleId;
    }

    public void setSourceArticleId(String sourceArticleId) {
        this.sourceArticleId = sourceArticleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getLocationTagRaw() {
        return locationTagRaw;
    }

    public void setLocationTagRaw(String locationTagRaw) {
        this.locationTagRaw = locationTagRaw;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public double getLocationConfidence() {
        return locationConfidence;
    }

    public void setLocationConfidence(double locationConfidence) {
        this.locationConfidence = locationConfidence;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(Instant ingestedAt) {
        this.ingestedAt = ingestedAt;
    }
}
