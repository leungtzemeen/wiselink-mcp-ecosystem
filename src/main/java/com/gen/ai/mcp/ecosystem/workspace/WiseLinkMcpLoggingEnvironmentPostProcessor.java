package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import lombok.extern.slf4j.Slf4j;

/**
 * 在 Logback 初始化之前，将 {@code wiselink.mcp.log-directory} 设为「工作区根/logs」的绝对路径，
 * 与 {@link WiseLinkWorkspaceRootResolver} 规则一致。
 */
@Slf4j
public class WiseLinkMcpLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "wiselinkMcpLogging";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        WiseLinkWorkspaceRootResolver.Resolution resolution =
                WiseLinkWorkspaceRootResolver.resolveWithSource(environment);
        WiseLinkWorkspaceRootResolver.logWorkspaceResolution(
                "(pre-logback, MCP file logging)",
                resolution);

        Path root = resolution.root();
        Path logDir = root.resolve("logs").toAbsolutePath().normalize();
        environment.getPropertySources()
                .addFirst(new MapPropertySource(
                        PROPERTY_SOURCE_NAME,
                        Map.of(WiseLinkWorkspaceRootResolver.LOG_DIRECTORY_PROPERTY, logDir.toString())));

        log.info(
                "{} (pre-logback) wiselink.mcp.log-directory -> {}",
                WiseLinkWorkspaceRootResolver.LOG_PREFIX,
                logDir);
    }
}
