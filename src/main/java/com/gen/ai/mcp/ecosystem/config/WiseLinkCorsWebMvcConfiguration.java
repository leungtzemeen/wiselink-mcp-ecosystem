package com.gen.ai.mcp.ecosystem.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(WiseLinkWebCorsProperties.class)
@ConditionalOnProperty(prefix = "wiselink.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WiseLinkCorsWebMvcConfiguration implements WebMvcConfigurer {

    private final WiseLinkWebCorsProperties corsProperties;

    public WiseLinkCorsWebMvcConfiguration(WiseLinkWebCorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String pattern = corsProperties.getPathPattern();
        if (!StringUtils.hasText(pattern)) {
            pattern = "/**";
        }
        CorsRegistration registration = registry.addMapping(pattern);
        applyPatterns(registration, corsProperties.getAllowedOriginPatterns());
        applyMethods(registration, corsProperties.getAllowedMethods());
        applyHeaders(registration, corsProperties.getAllowedHeaders());
        registration.allowCredentials(corsProperties.isAllowCredentials());
        Long maxAge = corsProperties.getMaxAgeSeconds();
        if (maxAge != null && maxAge >= 0) {
            registration.maxAge(maxAge);
        }
    }

    private static void applyPatterns(CorsRegistration registration, List<String> values) {
        if (!CollectionUtils.isEmpty(values)) {
            registration.allowedOriginPatterns(values.toArray(String[]::new));
        }
    }

    private static void applyMethods(CorsRegistration registration, List<String> values) {
        if (!CollectionUtils.isEmpty(values)) {
            registration.allowedMethods(values.toArray(String[]::new));
        }
    }

    private static void applyHeaders(CorsRegistration registration, List<String> values) {
        if (!CollectionUtils.isEmpty(values)) {
            registration.allowedHeaders(values.toArray(String[]::new));
        }
    }
}
