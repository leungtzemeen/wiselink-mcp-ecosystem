package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.StringUtils;

/**
 * MCP Server 工作区路径：PDF 导出目录为 {@code static/exports/}（即
 * {@code ${WISELINK_MCP_WORKSPACE_ROOT}/static/exports}），相对于 {@link #projectRoot()}。根目录由
 * {@link WiseLinkWorkspaceRootConfigurer} 在启动时解析（环境变量
 * {@value WiseLinkWorkspaceRootResolver#WORKSPACE_ROOT_ENV} &gt; {@code wiselink.mcp.workspace-root} &gt;
 * {@code user.dir}）；若在 Spring 初始化之前调用 {@link #projectRoot()}，则仅使用环境变量与 {@code user.dir} 的回退逻辑。
 */
public final class WiseLinkWorkspacePaths {

    /** PDF 导出目录：{@code static/exports}（相对工作区根）。 */
    public static final String STATIC_SUBDIR = "static";

    public static final String EXPORTS_SUBDIR = "exports";

    private static volatile Path resolvedProjectRoot;

    private WiseLinkWorkspacePaths() {
    }

    /**
     * 由 {@link WiseLinkWorkspaceRootConfigurer} 在启动时调用；合并配置后的绝对逻辑根。
     */
    static void setResolvedProjectRoot(Path root) {
        resolvedProjectRoot = root.toAbsolutePath().normalize();
    }

    public static Path projectRoot() {
        Path cached = resolvedProjectRoot;
        if (cached != null) {
            return cached;
        }
        return resolveBeforeSpringContext();
    }

    /**
     * Spring 上下文尚未就绪时（极少见）：仅环境变量 + {@code user.dir}。
     */
    private static Path resolveBeforeSpringContext() {
        String fromEnv = System.getenv(WiseLinkWorkspaceRootResolver.WORKSPACE_ROOT_ENV);
        if (StringUtils.hasText(fromEnv)) {
            Path p = Paths.get(fromEnv.trim());
            Path userDir = Paths.get(System.getProperty("user.dir", "."));
            Path root = p.isAbsolute() ? p : userDir.resolve(p);
            return root.toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
    }

    public static Path exportsDirectory() {
        return projectRoot().resolve(STATIC_SUBDIR).resolve(EXPORTS_SUBDIR);
    }
}
