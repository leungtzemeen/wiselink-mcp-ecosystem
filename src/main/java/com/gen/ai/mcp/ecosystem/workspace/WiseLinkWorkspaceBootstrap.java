package com.gen.ai.mcp.ecosystem.workspace;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 启动时预创建 {@code static/exports/}，与工具内 {@link Files#createDirectories} 行为一致且幂等。
 */
@Component
@Order(0)
@DependsOn("wiseLinkWorkspaceRootConfigurer")
@Slf4j
public class WiseLinkWorkspaceBootstrap implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path exports = WiseLinkWorkspacePaths.exportsDirectory();
        Files.createDirectories(exports);
        log.info("WiseLink workspace directory ready: exports={}", exports.toAbsolutePath());
    }
}
