package com.gen.ai.mcp.ecosystem.tools;

import java.net.URI;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * WiseLink 外部检索与网页正文抓取（MCP Server 侧仅暴露 {@link #scrapeWebsiteContent}）。
 */
@Service
@Slf4j
public class WiseLinkExternalSearchService {

    private static final int SCRAPE_TIMEOUT_MS = 12_000;
    /** 抓取正文最大字符数，避免注入对话的文本过长导致 Token 溢出。 */
    private static final int SCRAPE_MAX_TEXT_CHARS = 2_000;
    private static final String USER_AGENT = "WiseLinkBot/2.0 (+https://example.invalid/wiselink; external-search)";

    public record UrlRequest(String url) {
    }

    @Tool(name = "scrapeWebsiteContent", description = WiseLinkMcpToolDescriptions.SCRAPE_WEBSITE_CONTENT)
    public String scrapeWebsiteContent(UrlRequest request) {
        try {
            String raw = request == null || request.url() == null ? "" : request.url().trim();
            if (raw.isEmpty()) {
                return "错误：scrapeWebsiteContent 需要非空的 url。";
            }
            URI uri;
            try {
                uri = URI.create(raw);
            } catch (IllegalArgumentException ex) {
                return "错误：URL 无法解析 — " + ex.getMessage();
            }
            String scheme = uri.getScheme();
            if (scheme == null || !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                return "错误：仅允许 http 或 https URL。";
            }
            log.info(">>>> [WiseLink-External] Jsoup 抓取 url='{}'", uri);

            try {
                Document doc = Jsoup.connect(uri.normalize().toASCIIString())
                        .userAgent(USER_AGENT)
                        .timeout(SCRAPE_TIMEOUT_MS)
                        .maxBodySize(2_000_000)
                        .followRedirects(true)
                        .ignoreHttpErrors(false)
                        .get();

                doc.select("script, style, noscript, svg, iframe, header, footer, nav").remove();
                String title = Objects.requireNonNullElse(doc.title(), "").strip();
                String bodyText = doc.body() != null ? doc.body().text() : "";
                bodyText = bodyText == null ? "" : bodyText.replace('\u00a0', ' ').strip();
                bodyText = collapseWhitespace(bodyText);

                String core;
                if (title.isEmpty()) {
                    core = bodyText;
                } else {
                    core = "标题: " + title + "\n\n正文:\n" + bodyText;
                }
                if (core.length() > SCRAPE_MAX_TEXT_CHARS) {
                    core = core.substring(0, SCRAPE_MAX_TEXT_CHARS)
                            + "\n…（已截断至前 "
                            + SCRAPE_MAX_TEXT_CHARS
                            + " 字符，防止 Token 溢出）";
                }
                return core;
            } catch (Exception ex) {
                log.warn(">>>> [WiseLink-External] 抓取失败 url='{}': {}", uri, ex.toString());
                return "抓取失败（对话可继续；可在合规前提下重试或更换 URL）：" + ex.getMessage();
            }
        } catch (Exception ex) {
            log.warn(">>>> [WiseLink-External] scrapeWebsiteContent 外层异常: {}", ex.toString());
            return "网页抓取流程异常（对话可继续）：" + ex.getMessage();
        }
    }

    private static String collapseWhitespace(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return s.replaceAll("[ \\t\\x0B\\f\\r]+", " ").replaceAll("\\n{3,}", "\n\n").strip();
    }
}
