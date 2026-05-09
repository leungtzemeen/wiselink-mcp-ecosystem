package com.gen.ai.mcp.ecosystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.gen.ai.mcp.ecosystem.config.AppExportProperties;
import com.gen.ai.mcp.ecosystem.workspace.WiselinkMcpProperties;

/**
 * WiseLink MCP Ecosystem 入口：扫描 {@code com.gen.ai.mcp.ecosystem} 下的 Spring 组件与工具 Bean。
 */
@SpringBootApplication
@EnableConfigurationProperties({WiselinkMcpProperties.class, AppExportProperties.class})
public class WiselinkMcpEcosystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WiselinkMcpEcosystemApplication.class, args);
    }
}
