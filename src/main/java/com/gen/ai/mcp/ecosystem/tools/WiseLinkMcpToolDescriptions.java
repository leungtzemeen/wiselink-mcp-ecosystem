package com.gen.ai.mcp.ecosystem.tools;

/**
 * 与主工程 {@code WiseLinkTool} / {@code WiseLinkToolSecurityInterceptor}
 * 安全提示等约定对齐的工具描述常量，
 * 供 {@link org.springframework.ai.tool.annotation.Tool#description()}
 * 引用（编译期常量）。
 * {@link #EXPORT_SHOPPING_REPORT} 等与主工程导购导出约定同步演进；其余字段仍以主工程对应注解全文为准。
 */
public final class WiseLinkMcpToolDescriptions {

        private WiseLinkMcpToolDescriptions() {
        }

        /** 导购导出 PDF；成功时工具返回值为 PDF 绝对路径字符串（失败为 ERROR: 前缀）。 */
        public static final String EXPORT_SHOPPING_REPORT = "Generate a PDF shopping report based on contrast data. Input: recommendationText (Markdown format). Output: absolute file path. MANDATORY: Call this tool for any PDF/report requests; do NOT simulate.";

        /**
         * 与主工程 {@code WiseLinkExternalSearchService#scrapeWebsiteContent} 上
         * {@code @WiseLinkTool#description()} 全文一致。
         */
        public static final String SCRAPE_WEBSITE_CONTENT = "Scrape and clean web content from a URL. Returns title and main text. Use for real-time product reviews or latest pricing.";

        /**
         * 与主工程 {@code WiseLinkSystemToolsService#downloadExpertGuide} 上
         * {@code @WiseLinkTool#description()} 全文一致。
         */
        public static final String DOWNLOAD_EXPERT_GUIDE = "Download professional product guides. Input: guideName. Output: local download path.";
}
