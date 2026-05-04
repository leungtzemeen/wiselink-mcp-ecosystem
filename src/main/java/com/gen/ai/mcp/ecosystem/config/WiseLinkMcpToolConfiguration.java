package com.gen.ai.mcp.ecosystem.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gen.ai.mcp.ecosystem.tools.WiseLinkExportService;
import com.gen.ai.mcp.ecosystem.tools.WiseLinkExternalSearchService;
import com.gen.ai.mcp.ecosystem.tools.WiseLinkSystemToolsService;

/**
 * 将带 {@link org.springframework.ai.tool.annotation.Tool} 的工具 Bean 显式注册为
 * {@link ToolCallbackProvider}，供 MCP Server 聚合并对外声明工具列表（与主工程
 * {@code MethodToolCallback} 注册思路一致）。
 */
@Configuration
public class WiseLinkMcpToolConfiguration {

    @Bean
    public ToolCallbackProvider wiselinkEcosystemToolCallbackProvider(
            WiseLinkExportService exportService,
            WiseLinkExternalSearchService externalSearchService,
            WiseLinkSystemToolsService systemToolsService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(exportService, externalSearchService, systemToolsService)
                .build();
    }
}
