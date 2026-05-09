package com.gen.ai.mcp.ecosystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 对外下载 URL 拼装：{@code base-url}（通常由 {@code WISELINK_EXPORT_BASE_URL} 注入，到端口为止）+
 * {@code url-path}（与 {@link WiseLinkExportWebMvcConfiguration} 中 {@code /exports/**} 映射一致）。
 */
@ConfigurationProperties(prefix = "app.export")
public class AppExportProperties {

    /**
     * 协议 + 主机 + 端口，无尾部斜杠；生产环境通过环境变量 {@code WISELINK_EXPORT_BASE_URL} 覆盖。
     */
    private String baseUrl;

    /**
     * Web 静态映射路径前缀，须以 {@code /} 开头；建议以 {@code /} 结尾以便与文件名拼接，例如 {@code /exports/}。
     */
    private String urlPath;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "";
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath != null ? urlPath : "/exports/";
    }

    /**
     * 供 {@link org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry} 使用，例如
     * {@code /exports/**} 中的 {@code /exports}。
     */
    public String normalizedResourcePrefix() {
        String p = getUrlPath().trim();
        if (!StringUtils.hasText(p)) {
            return "/exports";
        }
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        while (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /**
     * 完整可点击下载 URL，例如 {@code http://localhost:8082/exports/report_xxx.pdf}。
     */
    public String buildDownloadUrl(String filename) {
        String base = getBaseUrl() == null ? "" : getBaseUrl().trim().replaceAll("/+$", "");
        String path = getUrlPath() == null ? "/exports/" : getUrlPath().trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return base + path + filename;
    }
}
