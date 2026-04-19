package com.fitcart.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "fitcart.cache")
public class CacheProperties {

    private boolean enabled = true;
    private boolean redisEnabled = true;
    private Duration advisorTopProductsTtl = Duration.ofMinutes(10);
    private Duration autocompleteTtl = Duration.ofMinutes(15);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public Duration getAdvisorTopProductsTtl() {
        return advisorTopProductsTtl;
    }

    public void setAdvisorTopProductsTtl(Duration advisorTopProductsTtl) {
        this.advisorTopProductsTtl = advisorTopProductsTtl;
    }

    public Duration getAutocompleteTtl() {
        return autocompleteTtl;
    }

    public void setAutocompleteTtl(Duration autocompleteTtl) {
        this.autocompleteTtl = autocompleteTtl;
    }
}
