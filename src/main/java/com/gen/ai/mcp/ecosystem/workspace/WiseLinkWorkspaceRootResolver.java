package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 与 {@link WiseLinkWorkspaceRootConfigurer} 一致的工作区根解析，供需要在极早阶段（如 Logback）消费的代码复用。
 */
@Slf4j
public final class WiseLinkWorkspaceRootResolver {

    /** 仅 MCP 子进程使用；优先级最高。 */
    public static final String WORKSPACE_ROOT_ENV = "WISELINK_MCP_WORKSPACE_ROOT";

    public static final String WORKSPACE_ROOT_PROPERTY = "wiselink.mcp.workspace-root";

    /**
     * 由 {@link WiseLinkMcpLoggingEnvironmentPostProcessor} 写入 Environment，供 {@code logback-spring.xml} 使用。
     */
    public static final String LOG_DIRECTORY_PROPERTY = "wiselink.mcp.log-directory";

    /** 统一运维检索前缀（与主工程约定对齐）。 */
    public static final String LOG_PREFIX = ">>>> [WiseLink-MCP-Workspace]";

    public record Resolution(Path root, String sourceKey) {
    }

    private WiseLinkWorkspaceRootResolver() {
    }

    /**
     * 解析工作区根及来源标识（用于诊断日志）。
     */
    public static Resolution resolveWithSource(ConfigurableEnvironment env) {
        String userDir = env.getProperty("user.dir", ".");
        Path userDirPath = Paths.get(userDir);

        String fromEnv = System.getenv(WORKSPACE_ROOT_ENV);
        if (StringUtils.hasText(fromEnv)) {
            Path root = normalizeRoot(Paths.get(fromEnv.trim()), userDirPath);
            return new Resolution(root, WORKSPACE_ROOT_ENV);
        }

        String fromConfig = env.getProperty(WORKSPACE_ROOT_PROPERTY);
        if (StringUtils.hasText(fromConfig)) {
            Path root = normalizeRoot(Paths.get(fromConfig.trim()), userDirPath);
            return new Resolution(root, WORKSPACE_ROOT_PROPERTY);
        }

        return new Resolution(userDirPath.normalize(), "user.dir");
    }

    public static Path resolveProjectRoot(ConfigurableEnvironment env) {
        return resolveWithSource(env).root();
    }

    /**
     * 输出一条解析结果日志（供 EnvironmentPostProcessor 等极早阶段调用；此时 Logback 可能尚未完全就绪）。
     */
    public static void logWorkspaceResolution(String context, Resolution resolution) {
        Path absolute = resolution.root().toAbsolutePath().normalize();
        log.info(
                "{} {} — workspace root source={} path={}",
                LOG_PREFIX,
                context,
                resolution.sourceKey(),
                absolute);
    }

    private static Path normalizeRoot(Path candidate, Path userDirPath) {
        Path root = candidate.isAbsolute() ? candidate : userDirPath.resolve(candidate);
        return root.normalize();
    }
}
