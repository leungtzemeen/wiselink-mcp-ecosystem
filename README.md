# WiseLink MCP Ecosystem

WiseLink 生态的 **MCP Server** 独立节点：与主工程 **Spring Boot 3.4.5 + Spring AI 1.1.3** 对齐。工具实现自 `gen-ai-agent` 的 `com.gen.ai.wiselink.tools` **无损平移**至 `com.gen.ai.mcp.ecosystem.tools`，`@Tool` 的 `name` / `description` 与主工程 `@WiseLinkTool` 拼接结果一致。

主应用通过 **stdio** 拉起本 JAR 作为子进程；本服务同时在 **8082** 提供 HTTP（MCP WebMVC、PDF 静态下载等）。

## 核心能力

- **Spring AI MCP Server**：`spring-ai-starter-mcp-server-webmvc`，SYNC + stdio，与 WiseLink 主工程 AI 导购链路集成。
- **选购报告导出**：`exportShoppingReport` 将 Agent 输出的 **Markdown**（含 **GFM 表格**、加粗等）经 **commonmark-java** 转 HTML，再由 **Flying Saucer + OpenPDF** 渲染为带版式的 PDF。
- **中文与版式**：多路径中文字体解析（系统字体 / `WISELINK_PDF_FONT` / classpath 字体），页眉页脚、表格边框与标题层级通过内嵌 CSS 控制。
- **工作区隔离**：日志与 PDF 统一落在 `{工作区根}/logs`、`{工作区根}/static/exports`，由环境变量 `WISELINK_MCP_WORKSPACE_ROOT` 驱动，适合主进程拉起子进程的场景。
- **可运维部署**：打包时 **JAR 内排除** `application.yml`、`logback-spring.xml`，**复制到** `target/config`，便于生产改配置而不重打镜像。
- **跨域与下载**：`wiselink.web.cors` 支持浏览器跨域访问 8082；`/exports/**` 映射工作区 PDF，工具返回可点击 `url`。

## 架构概览

```text
WiseLink 主应用 (8081)
    │  stdio 拉起子进程
    ▼
wiselink-mcp-ecosystem (本仓库, 8082)
    ├── MCP 工具: exportShoppingReport
    │       └── Markdown → HTML → PDF → {工作区}/static/exports/
    └── HTTP: /exports/** 下载 + MCP WebMVC
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 运行时 | Java 21、Spring Boot 3.4.5 |
| MCP | Spring AI 1.1.3 · `spring-ai-starter-mcp-server-webmvc` |
| Markdown | commonmark-java 0.22、**commonmark-ext-gfm-tables**（GFM 表格） |
| PDF | Flying Saucer 9.3（`flying-saucer-pdf-openpdf`）+ OpenPDF 2.0.3 |
| 其它 | Lombok、Jackson（工具返回 JSON） |

核心实现类：`WiseLinkExportService`（导出）、`WiseLinkExportWebMvcConfiguration`（静态下载）、`WiseLinkCorsWebMvcConfiguration`（CORS）。

## 快速开始

部署前请查看 **`application.yml` 顶部说明** 与 **`.env.example`**。生产环境务必设置 **`WISELINK_MCP_WORKSPACE_ROOT`**（绝对路径）。

```bash
mvn -DskipTests package
```

打包后：

- 可执行 JAR：`target/wiselink-mcp-ecosystem-0.1.0-SNAPSHOT.jar`
- 外置配置：`target/config/application.yml`、`target/config/logback-spring.xml`

**推荐启动方式**（使用外置配置，与 `pom.xml` 中 `maven-jar-plugin` / `copy-resources` 一致）：

```bash
java -jar target/wiselink-mcp-ecosystem-0.1.0-SNAPSHOT.jar \
  --spring.config.additional-location=file:./target/config/
```

若将 `config/` 与 JAR 放在同一目录部署，可改为 `file:./config/`。

默认 **HTTP 端口**：`8082`。应用名：`wiselink-mcp-ecosystem`。

- PDF 写入 **`{工作区根}/static/exports`**
- 通过 **`/exports/**`** 提供下载（`WiseLinkExportWebMvcConfiguration`）
- 工具返回的 **`url`** 由 `app.export.base-url` + `app.export.url-path` 拼装（可用 **`WISELINK_EXPORT_BASE_URL`** 覆盖 base-url）
- 启动时 `WiseLinkWorkspaceBootstrap` 预创建工作区目录

## 环境变量

| 变量 | 必填 | 说明 |
|------|------|------|
| `WISELINK_MCP_WORKSPACE_ROOT` | 生产建议必填 | 工作区根（绝对路径）。日志：`{根}/logs`；PDF：`{根}/static/exports` |
| `WISELINK_EXPORT_BASE_URL` | 视部署而定 | 下载链接中的协议+主机+端口，如 `http://localhost:8082` |
| `WISELINK_PDF_FONT` | 否 | 导出 PDF 用字体路径；TTC 需带索引，如 `C:/Windows/Fonts/msyh.ttc,0` |

示例见 **`.env.example`**。

## 工作区根目录

| 优先级 | 来源 |
|--------|------|
| 1 | 环境变量 **`WISELINK_MCP_WORKSPACE_ROOT`**（非空则作为根目录，可为绝对路径） |
| 2 | 配置 **`wiselink.mcp.workspace-root`**（默认绑定 `${WISELINK_MCP_WORKSPACE_ROOT}`，见 `application.yml`；相对路径时相对于 `user.dir`） |
| 3 | **`user.dir`**（JVM 工作目录） |

**主工程**通过 stdio 拉起本 JAR 时，建议注入 **`WISELINK_MCP_WORKSPACE_ROOT`**，避免子进程 `user.dir` 与主应用预期不一致。

仓库根下 `exports/`（若存在）仅作占位；实际写入路径以上表为准。

### 日志目录

文件日志（`ecosystem.log` 及滚动）写在 **`{工作区根}/logs`**。`WiseLinkMcpLoggingEnvironmentPostProcessor` 在 Logback 初始化前注入 `wiselink.mcp.log-directory`；`logback-spring.xml` 通过 `<springProperty>` 引用。

## 工具与入参

| 工具名 | 实现类 | 入参 |
|--------|--------|------|
| `exportShoppingReport` | `WiseLinkExportService` | JSON 字段 **`recommendationText`**（String，**完整 Markdown 正文**） |

调用示例形状：

```json
{"recommendationText": "# 选购建议\n\n| 型号 | 价格 |\n|------|------|\n| A | 100 |"}
```

成功时返回 JSON：`{"status":"success","url":"..."}`；失败时 `{"status":"error","message":"..."}`。

描述文案集中在 **`WiseLinkMcpToolDescriptions`**，与主工程约定同步（含同会话多次导出须使用新 `url` 等约束）。

### PDF 生成流程

1. 校验并截断超长正文（上限见 `WiseLinkExportService` 内 `MAX_BODY_CHARS`）。
2. **commonmark-java** 解析 Markdown，启用 **GFM Tables** 扩展。
3. 嵌入 XHTML 模板（标题区 + CSS：表格边框、标题层级、引用与代码块等）。
4. **Flying Saucer** 渲染 PDF，注册中文字体族 `WiseLinkCJK`；页眉页脚由 `ReportPageDecoration` 绘制。

## 配置说明

主配置：`src/main/resources/application.yml`（开发）；生产可使用 **`target/config/`** 外置副本。

| 配置前缀 | 作用 |
|----------|------|
| `spring.ai.mcp.server` | MCP 服务名、SYNC、stdio |
| `wiselink.mcp` | 工作区根、日志目录占位 |
| `wiselink.web.cors` | 跨域（默认 `/**`，生产建议收窄 `allowed-origin-patterns`） |
| `app.export` | 对外下载 URL 拼装、`/exports/` 路径前缀 |
| `server.port` | 默认 `8082` |

## 打包说明

`mvn package` 时：

- **`maven-jar-plugin`**：JAR 内**不包含** `application.yml`、`logback-spring.xml`
- **`maven-resources-plugin`**：将 `application*.yml`、`logback*.xml` **复制到** `target/config/`
- **`maven-surefire-plugin`**：默认 `skipTests=true`（可按需在命令行覆盖）

便于运维在部署目录只改 `config/`，无需重新编译业务 JAR。

## 仓库结构（简要）

```text
src/main/java/com/gen/ai/mcp/ecosystem/
  tools/          # MCP 工具（导出 PDF）
  config/         # 导出 URL、静态资源映射、CORS、工具 Bean
  workspace/      # 工作区根解析、日志目录、启动预创建目录
src/main/resources/
  application.yml
  logback-spring.xml
```
