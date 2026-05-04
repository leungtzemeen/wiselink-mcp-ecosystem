package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.StringUtils;

/**
 * MCP Server 工作区路径：{@code exports/}、{@code downloads/} 均相对于
 * {@link #projectRoot()}。根目录由 {@link WiseLinkWorkspaceRootConfigurer} 在启动时解析
 * （环境变量 {@value WiseLinkWorkspaceRootResolver#WORKSPACE_ROOT_ENV} &gt; {@code wiselink.mcp.workspace-root} &gt; {@code user.dir}）；
 * 若在 Spring 初始化之前调用 {@link #projectRoot()}，则仅使用环境变量与 {@code user.dir} 的回退逻辑。
 */
public final class WiseLinkWorkspacePaths {

    /** 与 {@code WiseLinkExportService} 原 {@code EXPORT_SUBDIR} 一致。 */
    public static final String EXPORTS_SUBDIR = "exports";

    /** 与 {@code WiseLinkSystemToolsService} 原 {@code DOWNLOADS_SUBDIR} 一致。 */
    public static final String DOWNLOADS_SUBDIR = "downloads";

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
        return projectRoot().resolve(EXPORTS_SUBDIR);
    }

    public static Path downloadsDirectory() {
        return projectRoot().resolve(DOWNLOADS_SUBDIR);
    }
}
