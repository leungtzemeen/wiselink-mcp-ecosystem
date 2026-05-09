package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 在 Logback 初始化之前，将 {@code wiselink.mcp.log-directory} 设为「工作区根/logs」的绝对路径，
 * 与 {@link WiseLinkWorkspaceRootResolver} 规则一致。
 * <p>
 * <strong>禁止在本类中使用 Slf4j</strong>：在 {@code ApplicationEnvironmentPreparedEvent} 阶段若调用
 * {@code log.info}，可能在 {@code logback-spring.xml} 尚未用 Environment 解析 {@code wiselink.mcp.log-directory}
 * 前就触发日志桥接，导致 {@code RollingFileAppender} 长期使用 {@code springProperty} 的
 * {@code defaultValue}（子进程 {@code user.dir} 常仍指向父工程目录）。
 * <p>
 * {@link Ordered#LOWEST_PRECEDENCE}：在 ConfigData 载入 {@code application.yml} 之后再 {@code addFirst}，
 * 与 yaml 中的 {@code wiselink.mcp.log-directory} 占位对齐并最终以解析结果覆盖。
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class WiseLinkMcpLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "wiselinkMcpLogging";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        WiseLinkWorkspaceRootResolver.Resolution resolution =
                WiseLinkWorkspaceRootResolver.resolveWithSource(environment);
        WiseLinkWorkspaceRootResolver.logWorkspaceResolutionToStderr("(pre-logback, MCP file logging)", resolution);

        Path root = resolution.root();
        Path logDir = root.resolve("logs").toAbsolutePath().normalize();
        environment.getPropertySources()
                .addFirst(new MapPropertySource(
                        PROPERTY_SOURCE_NAME,
                        Map.of(WiseLinkWorkspaceRootResolver.LOG_DIRECTORY_PROPERTY, logDir.toString())));

        System.err.println(
                WiseLinkWorkspaceRootResolver.LOG_PREFIX
                        + " (pre-logback) wiselink.mcp.log-directory -> "
                        + logDir);
    }
}
