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

默认 **HTTP 端口**：`8082`。应用名：`wiselink-mcp-ecosystem`。`exports/`、`downloads/` 位于 **MCP 工作区根** 下；启动时由 `WiseLinkWorkspaceBootstrap` 预创建这两目录。

### 工作区根目录（仅本 MCP 子进程）

| 优先级 | 来源 |
|--------|------|
| 1 | 环境变量 **`WISELINK_MCP_WORKSPACE_ROOT`**（非空则作为根目录，可为绝对路径） |
| 2 | 配置 **`wiselink.mcp.workspace-root`**（`application.yml`；相对路径时相对于 `user.dir`） |
| 3 | **`user.dir`**（JVM 工作目录，与未配置时行为一致） |

**主工程**在通过 stdio 拉起本 JAR 时，建议 **注入 `WISELINK_MCP_WORKSPACE_ROOT`** 指向期望的数据目录，避免依赖子进程默认 `user.dir` 导致导出路径与预期不符。

仓库根下已保留带 `.gitkeep` 的 `exports/`、`downloads/` 仅作目录占位；实际写入路径以上表解析结果为准。

### 日志目录

文件日志（`ecosystem.log` 及滚动文件）写在 **`{工作区根}/logs`**，与 `exports/`、`downloads/` 同盘同根。工作区根解析规则见上表；实现上由 `WiseLinkMcpLoggingEnvironmentPostProcessor` 在 Logback 初始化前注入 `wiselink.mcp.log-directory`，`logback-spring.xml` 通过 `<springProperty>` 引用。未走 Spring 启动的极端场景下回退为 `${user.dir}/logs`。

## 工具与入参形状

| 工具名 | 类 | 入参类型（与主工程一致） |
|--------|-----|---------------------------|
| `exportShoppingReport` | `WiseLinkExportService` | `ShoppingReportExportRequest(recommendationText)` |
| `scrapeWebsiteContent` | `WiseLinkExternalSearchService` | `UrlRequest(url)` |
| `downloadExpertGuide` | `WiseLinkSystemToolsService` | `ExpertGuideDownloadRequest(fileUrl, fileName)` |

描述文案集中在 `WiseLinkMcpToolDescriptions`，与主工程逐字对应。

## 配置

见 `src/main/resources/application.yml`（MCP stdio、`wiselink.mcp.workspace-root` 等）。
