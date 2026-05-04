package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 启动时预创建 {@code exports/} 与 {@code downloads/}，与工具内 {@link Files#createDirectories} 行为一致且幂等。
 */
@Component
@Order(0)
@Slf4j
public class WiseLinkWorkspaceBootstrap implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path exports = WiseLinkWorkspacePaths.exportsDirectory();
        Path downloads = WiseLinkWorkspacePaths.downloadsDirectory();
        Files.createDirectories(exports);
        Files.createDirectories(downloads);
        log.info(
                "WiseLink workspace directories ready: exports={}, downloads={}",
                exports.toAbsolutePath(),
                downloads.toAbsolutePath());
    }
}
