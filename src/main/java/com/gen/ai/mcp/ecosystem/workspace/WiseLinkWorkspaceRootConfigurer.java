package com.gen.ai.mcp.ecosystem.workspace;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 在容器启动早期解析工作区根：环境变量 {@value WiseLinkWorkspaceRootResolver#WORKSPACE_ROOT_ENV} &gt;
 * {@value WiseLinkWorkspaceRootResolver#WORKSPACE_ROOT_PROPERTY} &gt; {@code user.dir}。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class WiseLinkWorkspaceRootConfigurer implements InitializingBean {

    private final ConfigurableEnvironment environment;

    @Override
    public void afterPropertiesSet() {
        WiseLinkWorkspaceRootResolver.Resolution resolution =
                WiseLinkWorkspaceRootResolver.resolveWithSource(environment);
        WiseLinkWorkspaceRootResolver.logWorkspaceResolution(
                "(exports/downloads tools)",
                resolution);
        WiseLinkWorkspacePaths.setResolvedProjectRoot(resolution.root());
    }
}
