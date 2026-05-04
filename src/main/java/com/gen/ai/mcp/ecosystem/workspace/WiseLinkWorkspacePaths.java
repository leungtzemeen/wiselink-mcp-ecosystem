package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 与主工程一致的相对目录名及工作区根路径解析（{@code user.dir}，即进程工作目录）。
 */
public final class WiseLinkWorkspacePaths {

    /** 与 {@code WiseLinkExportService} 原 {@code EXPORT_SUBDIR} 一致。 */
    public static final String EXPORTS_SUBDIR = "exports";

    /** 与 {@code WiseLinkSystemToolsService} 原 {@code DOWNLOADS_SUBDIR} 一致。 */
    public static final String DOWNLOADS_SUBDIR = "downloads";

    private WiseLinkWorkspacePaths() {
    }

    public static Path projectRoot() {
        return Paths.get(System.getProperty("user.dir", ".")).normalize();
    }

    public static Path exportsDirectory() {
        return projectRoot().resolve(EXPORTS_SUBDIR);
    }

    public static Path downloadsDirectory() {
        return projectRoot().resolve(DOWNLOADS_SUBDIR);
    }
}
