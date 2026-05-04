package com.gen.ai.mcp.ecosystem.tools;

/**
 * 与主工程 {@code WiseLinkTool} / {@code WiseLinkToolSecurityInterceptor} 安全提示等约定对齐的工具描述常量，
 * 供 {@link org.springframework.ai.tool.annotation.Tool#description()} 引用（编译期常量）。
 * {@link #EXPORT_SHOPPING_REPORT} 等与主工程导购导出约定同步演进；其余字段仍以主工程对应注解全文为准。
 */
public final class WiseLinkMcpToolDescriptions {

    private WiseLinkMcpToolDescriptions() {
    }

    /** 与 {@code WiseLinkToolSecurityInterceptor#TOOL_DESCRIPTION_SECURITY_NOTICE} 一致。 */
    public static final String TOOL_DESCRIPTION_SECURITY_NOTICE = "注意：此工具受权限控制，非 VIP 会话将调用失败。";

    /** 与 {@code WiseLinkExternalSearchService#TOOL_GUARDRAIL} 一致。 */
    public static final String EXTERNAL_SEARCH_TOOL_GUARDRAIL =
            "【调用约束 — 务必遵守】仅在本地知识不足或用户明确要求全网对比（例如明确提及「全网」「网上」「站外」「比价」「横向对比多家」等）时才可触发；"
                    + "严禁因重复试探、过度兜底或与本工具无关的闲聊而频繁调用，以节省 Token 与外部资源。"
                    + "普通站内导购、本地知识/RAG 足以回答的问题禁止调用。"
                    + " ";

    /** 导购导出 PDF；与主工程 {@code WiseLinkExportService#exportShoppingReport} 描述约定同步演进（含同会话多次导出约束）。 */
    public static final String EXPORT_SHOPPING_REPORT =
            "将完整的选购建议、对比结论或方案总结导出为 PDF 文件。"
                    + "当用户表示想保存方案、发给家人朋友查看、留档、打印，或需要一份正式的选购建议书 / 简报时，请主动调用本工具；"
                    + "入参 recommendationText 应填入当前对话中已形成的可读选购结论（可分条、含预算与备选型号等）。"
                    + "调用成功后请将返回的文件路径告知用户，便于其在 exports 目录下打开该 PDF。"
                    + "【同一会话多次导出 — 务必遵守】每次用户明确要求导出或更新 PDF 时均须重新调用本工具，不得以先前轮次代替；"
                    + "禁止仅凭历史对话臆测路径或文件名；告知用户的路径只能逐字引用本轮工具成功返回的原文。"
                    + TOOL_DESCRIPTION_SECURITY_NOTICE;

    /** 与主工程 {@code WiseLinkExternalSearchService#scrapeWebsiteContent} 上 {@code @WiseLinkTool#description()} 全文一致。 */
    public static final String SCRAPE_WEBSITE_CONTENT =
            EXTERNAL_SEARCH_TOOL_GUARDRAIL
                    + "根据用户明确提供的公开商品详情页 URL（仅 http/https），使用 Jsoup 抓取页面并抽取标题与正文纯文本，"
                    + "供跨站摘要与比价引用；不得对用户未给出的链接、内网或非 HTTP(S) 地址擅自抓取。"
                    + TOOL_DESCRIPTION_SECURITY_NOTICE;

    /** 与主工程 {@code WiseLinkSystemToolsService#downloadExpertGuide} 上 {@code @WiseLinkTool#description()} 全文一致。 */
    public static final String DOWNLOAD_EXPERT_GUIDE =
            "从用户提供的公开 HTTP(S) 链接下载资料到本地 downloads 目录，用于保存说明书、压缩包或演示视频等附件。"
                    + "仅允许安全的文件后缀：.pdf、.zip、.mp4。"
                    + "在已通过 exportShoppingReport 生成 PDF 选购报告后，若用户还需要下载相关产品高清画质演示片源或更详细的说明书/附件包，请主动引导其发起下载请求并调用本工具完成落地。"
                    + "成功后请将返回的本地路径告知用户。"
                    + TOOL_DESCRIPTION_SECURITY_NOTICE;
}
