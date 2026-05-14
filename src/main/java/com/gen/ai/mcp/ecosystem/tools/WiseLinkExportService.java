package com.gen.ai.mcp.ecosystem.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFCreationListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gen.ai.mcp.ecosystem.config.AppExportProperties;
import com.gen.ai.mcp.ecosystem.workspace.WiseLinkWorkspacePaths;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Rectangle;

import lombok.extern.slf4j.Slf4j;

/**
 * WiseLink 2.0：将选购建议导出为 PDF，供用户留存或分享（由 MCP Server 独立进程承载）。
 * 正文按 Markdown（含 GFM 表格）解析为 HTML，再由 Flying Saucer + OpenPDF 渲染为 PDF。
 */
@Service
@Slf4j
public class WiseLinkExportService {

    private static final String FS_FONT_FAMILY = "WiseLinkCJK";

    private static final List<Extension> MARKDOWN_EXTENSIONS = List.of(TablesExtension.create());

    private static final Parser MARKDOWN_PARSER = Parser.builder().extensions(MARKDOWN_EXTENSIONS).build();

    private static final HtmlRenderer MARKDOWN_HTML = HtmlRenderer.builder()
            .extensions(MARKDOWN_EXTENSIONS)
            .build();

    private final ObjectMapper objectMapper;
    private final AppExportProperties appExportProperties;

    public WiseLinkExportService(ObjectMapper objectMapper, AppExportProperties appExportProperties) {
        this.objectMapper = objectMapper;
        this.appExportProperties = appExportProperties;
    }

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
            .withLocale(Locale.ROOT);
    private static final DateTimeFormatter HEADER_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withLocale(Locale.ROOT);
    private static final int MAX_BODY_CHARS = 80_000;

    /** 可通过环境变量指定字体路径；TTC 需带索引，例如 {@code C:/Windows/Fonts/msyh.ttc,0}。 */
    private static final String ENV_FONT = "WISELINK_PDF_FONT";

    @Tool(name = "exportShoppingReport", description = WiseLinkMcpToolDescriptions.EXPORT_SHOPPING_REPORT)
    public String exportShoppingReport(String recommendationText) {
        Path tempFontCopy = null;
        try {
            String raw = recommendationText == null ? "" : recommendationText.trim();
            if (raw.isEmpty()) {
                return errorJson("exportShoppingReport 需要非空的 recommendationText（选购建议正文）。");
            }
            if (raw.length() > MAX_BODY_CHARS) {
                raw = raw.substring(0, MAX_BODY_CHARS)
                        + "\n\n…（正文已超过 "
                        + MAX_BODY_CHARS
                        + " 字符上限，后续内容已省略以保证导出稳定。）";
            }

            Path exportDir = WiseLinkWorkspacePaths.exportsDirectory();
            Files.createDirectories(exportDir);

            String stamp = LocalDateTime.now().format(FILE_TS);
            String filename = "wiselink-report-" + stamp + ".pdf";
            Path target = exportDir.resolve(filename);

            ChineseFontResolution fontResolution = resolveChineseFontResolution();
            tempFontCopy = fontResolution.tempFontFile();

            String headerInstant = LocalDateTime.now().format(HEADER_TS);
            String bodyHtml = markdownToHtmlFragment(raw);
            String xhtml = buildReportXhtml(bodyHtml, headerInstant, fontResolution.cjkReady());

            Path temp = Files.createTempFile("wiselink-report-", ".wip.pdf");
            try {
                try (OutputStream os = Files.newOutputStream(temp, StandardOpenOption.TRUNCATE_EXISTING)) {
                    ITextRenderer renderer = new ITextRenderer();
                    if (fontResolution.fsFontPath() != null) {
                        renderer.getFontResolver().addFont(
                                fontResolution.fsFontPath(),
                                FS_FONT_FAMILY,
                                BaseFont.IDENTITY_H,
                                true,
                                null);
                    }
                    renderer.setListener(new PDFCreationListener() {
                        @Override
                        public void preOpen(ITextRenderer r) {
                            r.getWriter().setPageEvent(new ReportPageDecoration(fontResolution.baseFont(), headerInstant));
                        }

                        @Override
                        public void preWrite(ITextRenderer r, int pageCount) {
                            // no-op
                        }

                        @Override
                        public void onClose(ITextRenderer r) {
                            // no-op
                        }
                    });
                    renderer.setDocumentFromString(xhtml, null);
                    renderer.layout();
                    renderer.createPDF(os);
                }
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                temp = null;
            } finally {
                if (temp != null) {
                    try {
                        Files.deleteIfExists(temp);
                    } catch (IOException ex) {
                        log.debug("删除临时 PDF 失败: {}", ex.getMessage());
                    }
                }
            }

            Path absolute = target.toAbsolutePath().normalize();
            if (!Files.isRegularFile(absolute) || Files.size(absolute) <= 0L) {
                return errorJson("PDF 已写入但校验失败（文件不存在或大小为 0）：" + absolute);
            }
            String downloadUrl = appExportProperties.buildDownloadUrl(filename);
            log.info(">>>> [Export-Success] PDF 已就绪，下载链接: {}", downloadUrl);
            log.info(">>>> [WiseLink-Export] PDF 已生成 path='{}'", absolute);
            return successJson(downloadUrl);
        } catch (Exception ex) {
            log.warn(">>>> [WiseLink-Export] 导出失败: {}", ex.toString());
            String detail = ex.getMessage();
            if (detail == null || detail.isBlank()) {
                detail = ex.toString();
            }
            return errorJson(detail);
        } finally {
            if (tempFontCopy != null) {
                try {
                    Files.deleteIfExists(tempFontCopy);
                } catch (IOException ex) {
                    log.debug("删除临时字体文件失败: {}", ex.getMessage());
                }
            }
        }
    }

    private static String markdownToHtmlFragment(String markdown) {
        Node doc = MARKDOWN_PARSER.parse(markdown);
        String html = MARKDOWN_HTML.render(doc);
        return polishHtmlFragmentForXhtml(html);
    }

    /**
     * Flying Saucer 需要良构 XML；CommonMark 输出的个别空元素在部分环境下可能非自闭合，这里做保守修补。
     */
    private static String polishHtmlFragmentForXhtml(String html) {
        String s = html;
        s = s.replace("<br>", "<br />");
        s = s.replace("<br >", "<br />");
        s = s.replace("<hr>", "<hr />");
        s = s.replace("<hr >", "<hr />");
        return s;
    }

    private static String buildReportXhtml(String bodyHtml, String headerInstant, boolean cjkReady) {
        String fontStack = cjkReady ? "\"" + FS_FONT_FAMILY + "\", serif" : "sans-serif";
        String notice = "";
        if (!cjkReady) {
            notice = "<div class=\"font-notice\">Notice: No suitable CJK font was found. Set environment variable "
                    + ENV_FONT
                    + " to a .ttf/.ttc path (TTC example: C:/Windows/Fonts/msyh.ttc,0), "
                    + "or add src/main/resources/fonts/*.ttf.</div>";
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                + "<head>\n"
                + "<meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=UTF-8\"/>\n"
                + "<style type=\"text/css\">\n"
                + "  @page { size: A4; }\n"
                + "  body {\n"
                + "    font-family: "
                + fontStack
                + ";\n"
                + "    font-size: 11pt;\n"
                + "    line-height: 1.45;\n"
                + "    color: #1a1a1a;\n"
                + "    margin: 0;\n"
                + "    padding: 62pt 48pt 40pt 48pt;\n"
                + "    text-align: justify;\n"
                + "  }\n"
                + "  .font-notice { font-size: 10pt; color: #a63a32; margin-bottom: 12pt; }\n"
                + "  .report-title-block { margin-bottom: 14pt; }\n"
                + "  .report-title {\n"
                + "    font-size: 16pt;\n"
                + "    font-weight: bold;\n"
                + "    text-align: center;\n"
                + "    margin: 0 0 6pt 0;\n"
                + "    letter-spacing: 0.02em;\n"
                + "  }\n"
                + "  .report-meta { font-size: 9.5pt; text-align: center; color: #555; margin: 0; }\n"
                + "  .markdown-body p { margin: 0 0 7pt 0; }\n"
                + "  .markdown-body h1 {\n"
                + "    font-size: 15pt;\n"
                + "    font-weight: bold;\n"
                + "    margin: 16pt 0 8pt 0;\n"
                + "    border-bottom: 0.4pt solid #d8d8d8;\n"
                + "    padding-bottom: 3pt;\n"
                + "  }\n"
                + "  .markdown-body h2 { font-size: 13pt; font-weight: bold; margin: 14pt 0 6pt 0; }\n"
                + "  .markdown-body h3 { font-size: 11.5pt; font-weight: bold; margin: 12pt 0 5pt 0; }\n"
                + "  .markdown-body h4, .markdown-body h5, .markdown-body h6 { font-size: 11pt; font-weight: bold; margin: 10pt 0 4pt 0; }\n"
                + "  .markdown-body ul, .markdown-body ol { margin: 6pt 0 8pt 20pt; padding: 0; }\n"
                + "  .markdown-body li { margin: 2pt 0; }\n"
                + "  .markdown-body blockquote {\n"
                + "    margin: 8pt 0 8pt 12pt;\n"
                + "    padding: 4pt 0 4pt 10pt;\n"
                + "    border-left: 2.5pt solid #c8ccd4;\n"
                + "    color: #444;\n"
                + "  }\n"
                + "  .markdown-body code { font-size: 10pt; }\n"
                + "  .markdown-body pre {\n"
                + "    font-size: 9.5pt;\n"
                + "    background: #f6f7f9;\n"
                + "    border: 0.35pt solid #dcdfe6;\n"
                + "    padding: 8pt;\n"
                + "    margin: 8pt 0;\n"
                + "    white-space: pre-wrap;\n"
                + "  }\n"
                + "  .markdown-body table {\n"
                + "    border-collapse: collapse;\n"
                + "    width: 100%;\n"
                + "    margin: 10pt 0 12pt 0;\n"
                + "    font-size: 10.5pt;\n"
                + "  }\n"
                + "  .markdown-body th, .markdown-body td {\n"
                + "    border: 0.45pt solid #3a3f4d;\n"
                + "    padding: 6pt 8pt;\n"
                + "    vertical-align: top;\n"
                + "  }\n"
                + "  .markdown-body th {\n"
                + "    background-color: #eef0f4;\n"
                + "    font-weight: bold;\n"
                + "    text-align: left;\n"
                + "  }\n"
                + "  .markdown-body strong { font-weight: bold; }\n"
                + "</style>\n"
                + "</head>\n"
                + "<body>\n"
                + notice
                + "<div class=\"report-title-block\">\n"
                + "<div class=\"report-title\">WiseLinkAI(智选灵犀) 选购建议书</div>\n"
                + "<div class=\"report-meta\">生成时间："
                + escapeXml(headerInstant)
                + "</div>\n"
                + "</div>\n"
                + "<div class=\"markdown-body\">\n"
                + bodyHtml
                + "\n</div>\n"
                + "</body>\n"
                + "</html>\n";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String successJson(String downloadUrl) throws JsonProcessingException {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "success");
        body.put("url", downloadUrl);
        return objectMapper.writeValueAsString(body);
    }

    private String errorJson(String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "error");
        body.put("message", message);
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            return "{\"status\":\"error\",\"message\":\"JSON 序列化失败\"}";
        }
    }

    private record ChineseFontResolution(
            BaseFont baseFont,
            /** 传给 Flying Saucer {@code addFont} 的本地路径；Helvetica 回退时为 null。 */
            String fsFontPath,
            /** 从 classpath 拷出的临时字体，需在导出结束后删除。 */
            Path tempFontFile,
            boolean cjkReady) {
    }

    /**
     * 与历史 {@code resolveChineseBaseFont} 相同的候选与回退顺序；额外返回供 HTML→PDF 使用的字体路径。
     */
    private ChineseFontResolution resolveChineseFontResolution() throws DocumentException, IOException {
        List<String> candidates = new ArrayList<>();
        String env = System.getenv(ENV_FONT);
        if (env != null && !env.isBlank()) {
            candidates.add(env.trim());
        }
        candidates.addAll(
                List.of(
                        "C:/Windows/Fonts/msyh.ttc,0",
                        "C:/Windows/Fonts/msyhbd.ttc,0",
                        "C:/Windows/Fonts/simsun.ttc,0",
                        "C:/Windows/Fonts/simhei.ttf",
                        "C:/Windows/Fonts/msyh.ttf",
                        "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                        "/usr/share/fonts/truetype/arphic/uming.ttc",
                        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0",
                        "/Library/Fonts/Arial Unicode.ttf",
                        "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
                        "/System/Library/Fonts/PingFang.ttc,0"));

        for (String spec : candidates) {
            Path pathOnly = fontPathOnly(spec);
            try {
                if (!Files.isRegularFile(pathOnly)) {
                    continue;
                }
                BaseFont bf = BaseFont.createFont(spec, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return new ChineseFontResolution(bf, spec, null, BaseFont.IDENTITY_H.equals(bf.getEncoding()));
            } catch (Exception ex) {
                log.debug("跳过不可用字体 spec='{}': {}", spec, ex.getMessage());
            }
        }

        try (InputStream in = WiseLinkExportService.class.getResourceAsStream("/fonts/NotoSansSC-Regular.ttf")) {
            if (in != null) {
                byte[] bytes = in.readAllBytes();
                BaseFont bf = BaseFont.createFont(
                        "NotoSansSC-Regular.ttf",
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED,
                        true,
                        bytes,
                        null);
                Path tmp = Files.createTempFile("wiselink-cjk-", ".ttf");
                Files.write(tmp, bytes);
                return new ChineseFontResolution(bf, tmp.toAbsolutePath().normalize().toString(), tmp, true);
            }
        }

        log.warn(
                "未找到可用的中文字体（已尝试常见系统路径与 classpath:/fonts/NotoSansSC-Regular.ttf）。"
                        + "请设置 {} 或自行放置 TTF。PDF 将退回 Helvetica，中文可能显示为空白或乱码。",
                ENV_FONT);
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        return new ChineseFontResolution(bf, null, null, false);
    }

    private static Path fontPathOnly(String fontSpec) {
        int comma = fontSpec.indexOf(',');
        String pathPart = comma >= 0 ? fontSpec.substring(0, comma) : fontSpec;
        return Paths.get(pathPart);
    }

    private static final class ReportPageDecoration extends PdfPageEventHelper {

        private final BaseFont bf;
        private final String generatedAt;

        ReportPageDecoration(BaseFont bf, String generatedAt) {
            this.bf = bf;
            this.generatedAt = generatedAt;
        }

        @Override
        public void onStartPage(PdfWriter writer, com.lowagie.text.Document document) {
            Rectangle ps = document.getPageSize();
            PdfContentByte cb = writer.getDirectContent();
            float left = ps.getLeft() + 48f;
            float right = ps.getRight() - 48f;
            float top = ps.getTop() - 26f;

            cb.saveState();
            cb.setLineWidth(0.4f);
            cb.moveTo(left, ps.getTop() - 34f);
            cb.lineTo(right, ps.getTop() - 34f);
            cb.stroke();
            cb.restoreState();

            cb.saveState();
            cb.beginText();
            cb.setFontAndSize(bf, 9f);
            cb.showTextAligned(Element.ALIGN_LEFT, "WiseLink 2.0 · 选购报告", left, top, 0f);
            cb.showTextAligned(Element.ALIGN_RIGHT, "生成时间 " + generatedAt, right, top, 0f);
            cb.endText();
            cb.restoreState();
        }

        @Override
        public void onEndPage(PdfWriter writer, com.lowagie.text.Document document) {
            Rectangle ps = document.getPageSize();
            PdfContentByte cb = writer.getDirectContent();
            float cx = (ps.getLeft() + ps.getRight()) / 2f;
            float footerY = ps.getBottom() + 28f;

            cb.saveState();
            cb.setLineWidth(0.35f);
            cb.moveTo(ps.getLeft() + 48f, ps.getBottom() + 38f);
            cb.lineTo(ps.getRight() - 48f, ps.getBottom() + 38f);
            cb.stroke();
            cb.restoreState();

            String footerText = "本文件由 WiseLink 导购助手自动生成 · 第 " + writer.getPageNumber() + " 页";

            cb.saveState();
            cb.beginText();
            cb.setFontAndSize(bf, 8.5f);
            cb.showTextAligned(Element.ALIGN_CENTER, footerText, cx, footerY, 0f);
            cb.endText();
            cb.restoreState();
        }
    }
}
