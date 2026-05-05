package com.gen.ai.mcp.ecosystem.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gen.ai.mcp.ecosystem.tools.WiseLinkExportService;
import com.gen.ai.mcp.ecosystem.tools.WiseLinkExternalSearchService;
import com.gen.ai.mcp.ecosystem.tools.WiseLinkSystemToolsService;

/**
 * 显式声明 {@link MethodToolCallbackProvider}，将导购 MCP 工具方法注册为 {@link ToolCallbackProvider}。
 * <p>
 * <strong>手动注册（无 classpath 扫描）</strong>：仅将下列三个 Spring Bean 传入
 * {@link MethodToolCallbackProvider.Builder#toolObjects(Object...)}，由其解析各 Bean 上带
 * {@link org.springframework.ai.tool.annotation.Tool} 的方法并生成 ToolCallback。
 * <ul>
 *   <li>{@link WiseLinkExportService}</li>
 *   <li>{@link WiseLinkExternalSearchService}</li>
 *   <li>{@link WiseLinkSystemToolsService}</li>
 * </ul>
 * 新增工具 Service 时须在本 Bean 方法参数与 {@code toolObjects} 中同步追加。
 * <p>
 * <strong>协议与元数据</strong>：Spring AI MCP Server 自动配置会收集容器中全部 {@link ToolCallbackProvider}
 * Bean，合并为对外工具列表与调用路由；本 Bean 即为此聚合链路的一环，用于避免客户端侧看不到完整 tool
 * schema / tool_calls 对应实现的情况（须与主工程 MCP 客户端配置一并验证）。
 */
@Configuration
public class WiseLinkMcpToolConfiguration {

    @Bean
    public ToolCallbackProvider wiselinkEcosystemToolCallbackProvider(
            WiseLinkExportService exportService,
            WiseLinkExternalSearchService externalSearchService,
            WiseLinkSystemToolsService systemToolsService) {
        MethodToolCallbackProvider methodToolCallbackProvider = MethodToolCallbackProvider.builder()
                .toolObjects(exportService, externalSearchService, systemToolsService)
                .build();
        return methodToolCallbackProvider;
    }
}
