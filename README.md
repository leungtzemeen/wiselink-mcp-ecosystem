# WiseLink MCP Ecosystem

WiseLink 生态的 **MCP Server** 节点：与主工程 **Spring Boot 3.4.5 + Spring AI 1.1.3** 对齐，工具实现自 `gen-ai-agent` 的 `com.gen.ai.wiselink.tools` **无损平移**至 `com.gen.ai.mcp.ecosystem.tools`，`@Tool` 的 `name` / `description` 与主工程 `@WiseLinkTool` 拼接结果一致。

## 技术栈

- **Java 17**、**Spring Boot 3.4.5**
- **Spring AI 1.1.3**：`spring-ai-starter-mcp-server-webmvc`（`spring-ai-bom` 管理版本）
- **OpenPDF**：`WiseLinkExportService#exportShoppingReport`
- **Jsoup**：`WiseLinkExternalSearchService#scrapeWebsiteContent`
- **JDK HttpClient**：`WiseLinkSystemToolsService#downloadExpertGuide`（与主工程 Hutool 行为等价，无 Hutool 依赖）
- **Lombok**：与主工程一致的 `@Slf4j`

## 快速开始

```bash
mvn -DskipTests package
java -jar target/wiselink-mcp-ecosystem-0.1.0-SNAPSHOT.jar
```

默认 **HTTP 端口**：`8082`。应用名：`wiselink-mcp-ecosystem`。请在**进程工作目录**为工程根时启动；启动时由 `WiseLinkWorkspaceBootstrap` 预创建 **`exports/`** 与 **`downloads/`**（与主工程一致，基于 `user.dir`）。仓库内已保留带 `.gitkeep` 的空目录供克隆后对齐。

## 工具与入参形状

| 工具名 | 类 | 入参类型（与主工程一致） |
|--------|-----|---------------------------|
| `exportShoppingReport` | `WiseLinkExportService` | `ShoppingReportExportRequest(recommendationText)` |
| `scrapeWebsiteContent` | `WiseLinkExternalSearchService` | `UrlRequest(url)` |
| `downloadExpertGuide` | `WiseLinkSystemToolsService` | `ExpertGuideDownloadRequest(fileUrl, fileName)` |

描述文案集中在 `WiseLinkMcpToolDescriptions`，与主工程逐字对应。

## 配置

见 `src/main/resources/application.yml`（`spring.ai.mcp.server.protocol: STREAMABLE`）。
