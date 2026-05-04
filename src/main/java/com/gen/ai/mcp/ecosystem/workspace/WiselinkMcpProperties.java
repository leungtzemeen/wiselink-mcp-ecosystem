package com.gen.ai.mcp.ecosystem.workspace;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MCP 节点专用配置（非 Spring AI 官方前缀，避免冲突）。
 */
@ConfigurationProperties(prefix = "wiselink.mcp")
public class WiselinkMcpProperties {

    /**
     * 工作区根目录；可为相对路径（相对于 {@code user.dir} 解析）。
     * 优先级低于环境变量 {@code WISELINK_MCP_WORKSPACE_ROOT}。
     */
    private String workspaceRoot = "";

    public String getWorkspaceRoot() {
        return workspaceRoot;
    }

    public void setWorkspaceRoot(String workspaceRoot) {
        this.workspaceRoot = workspaceRoot != null ? workspaceRoot : "";
    }
}
