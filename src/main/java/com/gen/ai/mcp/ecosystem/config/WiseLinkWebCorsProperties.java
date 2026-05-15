package com.gen.ai.mcp.ecosystem.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 浏览器跨域访问 8082（MCP HTTP、导出下载等）时的 CORS 设置；生产建议收窄
 * {@link #allowedOriginPatterns}。
 */
@ConfigurationProperties(prefix = "wiselink.web.cors")
public class WiseLinkWebCorsProperties {

    private boolean enabled = true;

    /** 与 {@link org.springframework.web.servlet.config.annotation.CorsRegistry#addMapping} 一致，默认整站。 */
    private String pathPattern = "/**";

    /** 使用 origin pattern（可与 {@code *} 通配）；与 {@link #allowCredentials} 为 true 时不要使用 {@code *}。 */
    private List<String> allowedOriginPatterns = List.of("*");

    private List<String> allowedMethods = List.of("*");

    private List<String> allowedHeaders = List.of("*");

    private boolean allowCredentials = false;

    private Long maxAgeSeconds = 3600L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(Long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }
}
