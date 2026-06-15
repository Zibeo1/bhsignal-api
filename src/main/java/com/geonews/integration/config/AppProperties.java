package com.geonews.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Webhook webhook = new Webhook();
    private final Cors cors = new Cors();
    private final Auth auth = new Auth();
    private final News news = new News();

    public Webhook getWebhook() {
        return webhook;
    }

    public Cors getCors() {
        return cors;
    }

    public Auth getAuth() {
        return auth;
    }

    public News getNews() {
        return news;
    }

    public static class Webhook {
        private String secret = "change-me";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:4200"));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Auth {
        private String tokenSecret = "change-me-token-secret";
        private long tokenTtlSeconds = 60 * 60 * 24 * 7;

        public String getTokenSecret() {
            return tokenSecret;
        }

        public void setTokenSecret(String tokenSecret) {
            this.tokenSecret = tokenSecret;
        }

        public long getTokenTtlSeconds() {
            return tokenTtlSeconds;
        }

        public void setTokenTtlSeconds(long tokenTtlSeconds) {
            this.tokenTtlSeconds = tokenTtlSeconds;
        }
    }

    public static class News {
        private boolean bosniaOnly = true;

        public boolean isBosniaOnly() {
            return bosniaOnly;
        }

        public void setBosniaOnly(boolean bosniaOnly) {
            this.bosniaOnly = bosniaOnly;
        }
    }
}
