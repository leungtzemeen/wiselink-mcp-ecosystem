package com.gen.ai.mcp.ecosystem.tools;

/**
 * 与主工程 {@code WiseLinkTool} / {@code WiseLinkToolSecurityInterceptor} 安全提示等约定对齐的工具描述常量，
 * 供 {@link org.springframework.ai.tool.annotation.Tool#description()} 引用（编译期常量）。
 */
public final class WiseLinkMcpToolDescriptions {

    private WiseLinkMcpToolDescriptions() {
    }

    /** 导购导出 PDF；与主工程 {@code WiseLinkExportService#exportShoppingReport} 描述约定同步演进（含同会话多次导出约束）。 */
    public static final String EXPORT_SHOPPING_REPORT =
            "将完整的选购建议、对比结论或方案总结导出为 PDF 文件。"
                    + "当用户表示想保存方案、发给家人朋友查看、留档、打印，或需要一份正式的选购建议书 / 简报时，请主动调用本工具；"
                    + "入参 recommendationText 应填入当前对话中已形成的可读选购结论（可分条、含预算与备选型号等）。"
                    + "调用成功后返回 JSON，含可点击的 HTTP 下载链接字段 url（由 app.export.base-url 与 url-path 拼装）；请将 url 原样告知用户。"
                    + "【同一会话多次导出 — 务必遵守】每次用户明确要求导出或更新 PDF 时均须重新调用本工具，不得以先前轮次代替；"
                    + "禁止仅凭历史对话臆测路径或文件名；告知用户的路径只能逐字引用本轮工具成功返回的原文。";
}
