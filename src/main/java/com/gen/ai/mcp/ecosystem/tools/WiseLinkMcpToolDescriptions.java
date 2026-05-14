package com.gen.ai.mcp.ecosystem.tools;

/**
 * 与主工程 {@code WiseLinkTool} / {@code WiseLinkToolSecurityInterceptor}
 * 安全提示等约定对齐的工具描述常量，
 * 供 {@link org.springframework.ai.tool.annotation.Tool#description()}
 * 引用（编译期常量）。
 */
public final class WiseLinkMcpToolDescriptions {

        private WiseLinkMcpToolDescriptions() {
        }

        /**
         * 导购导出 PDF；与主工程 {@code WiseLinkExportService#exportShoppingReport}
         * 描述约定同步演进（含同会话多次导出约束）。
         */
        public static final String EXPORT_SHOPPING_REPORT = "导出选购 PDF。 "
                        + "触发：用户要求导出一份文档、生成一份报告。 "
                        + "调用时 JSON 必须为：{\"recommendationText\":\"此处为完整 Markdown 正文\"}，其中值为详细对比表与结论（Markdown）,勿使用其它字段名。 "
                        + "约束：必须逐字引用本轮返回的新 url，严禁复用旧链接或臆测路径；方案更新必须重调。";
}
