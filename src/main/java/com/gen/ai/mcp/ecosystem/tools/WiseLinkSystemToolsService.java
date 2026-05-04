package com.gen.ai.mcp.ecosystem.tools;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import com.gen.ai.mcp.ecosystem.workspace.WiseLinkWorkspacePaths;

import lombok.extern.slf4j.Slf4j;

/**
 * WiseLink 2.0 系统类工具：受控资料下载（独立 MCP Server；不引入 Hutool，使用 JDK HttpClient 保持行为一致）。
 */
@Service
@Slf4j
public class WiseLinkSystemToolsService {

    private static final int DOWNLOAD_TIMEOUT_MS = 120_000;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public record ExpertGuideDownloadRequest(String fileUrl, String fileName) {
    }

    @Tool(name = "downloadExpertGuide", description = WiseLinkMcpToolDescriptions.DOWNLOAD_EXPERT_GUIDE)
    public String downloadExpertGuide(ExpertGuideDownloadRequest request) {
        try {
            String rawUrl = request == null || request.fileUrl() == null ? "" : request.fileUrl().trim();
            String rawName = request == null || request.fileName() == null ? "" : request.fileName().trim();
            if (rawUrl.isEmpty()) {
                return "错误：downloadExpertGuide 需要非空的 fileUrl（http/https）。";
            }

            URI uri;
            try {
                uri = URI.create(rawUrl);
            } catch (IllegalArgumentException ex) {
                return "错误：URL 无法解析 — " + ex.getMessage();
            }
            String scheme = uri.getScheme();
            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return "错误：仅允许 http 或 https 链接。";
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return "错误：URL 缺少有效主机名。";
            }

            String safeName = validateDownloadFileName(rawName);

            Path downloadDir = WiseLinkWorkspacePaths.downloadsDirectory();
            Files.createDirectories(downloadDir);
            Path target = downloadDir.resolve(safeName).normalize();
            if (!target.startsWith(downloadDir.normalize())) {
                return "错误：解析后的保存路径非法。";
            }

            long sizeBefore = Files.exists(target) ? Files.size(target) : -1L;

            HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(DOWNLOAD_TIMEOUT_MS))
                    .header("User-Agent", "WiseLink-MCP-Ecosystem/2.0 (+https://wiselink)")
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                return "下载失败（对话可继续）：HTTP " + status;
            }

            try (InputStream in = response.body()) {
                if (in == null) {
                    return "下载失败（对话可继续）：响应体为空";
                }
                Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            Path absolute = target.toAbsolutePath().normalize();
            log.info(
                    ">>>> [WiseLink-System] 已下载 url='{}' -> {} (previousSize={})",
                    uri.getHost(),
                    absolute,
                    sizeBefore);
            return "下载成功。文件绝对路径：" + absolute + "（请将该路径告知用户，位于项目 downloads 目录。）";
        } catch (IllegalArgumentException ex) {
            return "下载被拒绝：" + ex.getMessage();
        } catch (Exception ex) {
            log.warn(">>>> [WiseLink-System] downloadExpertGuide 失败: {}", ex.toString());
            return "下载失败（对话可继续）：" + ex.getMessage();
        }
    }

    private static String validateDownloadFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName 不能为空。");
        }
        String name = fileName.trim();
        if (name.contains("..") || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("文件名不能包含路径分隔符或 ..。");
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".pdf") || lower.endsWith(".zip") || lower.endsWith(".mp4"))) {
            throw new IllegalArgumentException("仅允许后缀 .pdf、.zip、.mp4。");
        }
        return name;
    }
}
