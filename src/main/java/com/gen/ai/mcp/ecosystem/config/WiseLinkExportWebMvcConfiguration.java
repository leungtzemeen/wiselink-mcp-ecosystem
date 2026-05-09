package com.gen.ai.mcp.ecosystem.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gen.ai.mcp.ecosystem.workspace.WiseLinkWorkspacePaths;

/**
 * 将 {@link AppExportProperties#getUrlPath()} 对应的 Web 路径（如 {@code /exports/**}）映射到
 * {@link WiseLinkWorkspacePaths#exportsDirectory()}，即 {@code ${WISELINK_MCP_WORKSPACE_ROOT}/static/exports}。
 * 使用 {@code file:} 协议以兼容 Windows 绝对路径。
 */
@Configuration
public class WiseLinkExportWebMvcConfiguration implements WebMvcConfigurer {

    private final AppExportProperties appExportProperties;

    public WiseLinkExportWebMvcConfiguration(AppExportProperties appExportProperties) {
        this.appExportProperties = appExportProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = appExportProperties.normalizedResourcePrefix();
        Path dir = WiseLinkWorkspacePaths.exportsDirectory();
        String location = dir.toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(prefix + "/**").addResourceLocations(location);
    }
}
