# 第一轮改造与验证结果

历史报告：此文件记录包含 CRUD 示例的第一轮实现。当前空白领域模板的设计与验证见 [Sample 模块报告](sample-module-implementation-report.md) 和 [模块说明](../project-sample/README.md)。

## 交付

已完成可运行的单体多模块骨架和可复制领域模块模板。按用户选择，本轮没有制作 Maven Archetype 发布包，没有提交或推送代码，也没有绑定 CI/CD。

运行 reactor：
- project-bootstrap：唯一启动入口、全局配置、显式 Bean 装配、Dockerfile。
- project-shared：领域无关的异常、响应、UUID、JSON、Redis 字符串 TTL 与租约锁。
- project-sample：api / adapter / application / domain / infrastructure，包含创建和查询 Sample 的最小闭环。
- templates/domain-module：完整分层包与 POM、复制和注册说明；不参与默认构建。

包名基线为 org.xaspire.project，父坐标 org.xaspire:project:0.1.0-SNAPSHOT。保留 Java 21 目标、Spring Boot 3.5.16、MyBatis-Plus 3.5.17、Flyway 11.20.3、ArchUnit 1.5.0 和 springdoc 2.8.17；基础设施按设计使用 PostgreSQL 17、Redis 7。

## 架构行为

- bootstrap 只扫描自己的包，通过 @Bean 连接 SampleRepository 和 MyBatis 实现、SampleFacade 和应用服务；@Import 导入 HTTP 适配器，@MapperScan 注册 Mapper。
- domain 仅依赖自身 domain 与 JDK 核心值类型。持久化 DO、Mapper、UUID TypeHandler 位于 infrastructure；事务位于 application。
- PostgreSQL UUID 参数与结果显式转换，TypeHandler 在 bootstrap 注册。这个映射已用真实插入和查询验证。
- 对外接口位于顶层 api，跨领域只能访问对方 api。全局 ArchUnit 检查内部层方向、领域纯净、共享方向、组合根和模块环。
- 输入经 api facade 进入用例；创建返回 HTTP 201、Location 和统一响应；非法输入返回400，缺失记录返回404。
- shared 技术包允许 Jackson/Redis 依赖；shared 不包含业务服务或模型，domain 不使用 shared 技术能力。
- Flyway 在 bootstrap 执行一次，SQL 由领域模块提供，公共历史表与仓库级唯一版本序列，领域表使用 sample schema。
- 安全配置允许内部 ERROR dispatch 正常返回错误状态；prod 默认拒绝业务接口，dev/integration 开放演示接口。

根 AGENTS.md 已保留，其现有规则要求修改前先讨论；当前架构契约记录在 docs/architecture.md。历史 plans/ 保留为历史记录，不作为运行配置依据。

## 环境契约

docs/dev-ops 按设计拆分环境 Compose、开发 override、应用 Compose 和 dev/prod env 示例。
- 环境 Compose 不发布数据库/Redis端口，开发 override 仅绑定127.0.0.1。
- app Compose 只运行 APP_IMAGE，不进行 Maven/Docker 构建。
- bootstrap/Dockerfile 读取先行构建的 JAR，使用非 root Java21运行时。
- dev 提供本地示例值；prod 连接变量必填，示例密码和 APP_IMAGE 留空。
- 同一 COMPOSE_PROJECT_NAME 连接环境网络与应用外部网络。
- env 中 SPRING_* 供直接运行 JVM 使用；app Compose 从 POSTGRES_*、REDIS_PASSWORD 和服务名生成连接信息，文档已说明覆盖关系。
- 本地 dev.env/prod.env 被 .gitignore 排除。

## 实际验证

| 检查 | 结果 |
| --- | --- |
| Maven Wrapper clean verify | 通过；默认测试16项 |
| 最终 Maven Wrapper -Pintegration verify | 通过；默认16项 + 集成5项，共21项，零失败/零错误 |
| 应用用例 | 创建/查询、名称规范化、非法名称拒绝、稳定缺失错误码 |
| 架构检查 | 6条生产规则，7个合法/违规样例检查 |
| 真实 PostgreSQL | Flyway迁移、UUID插入查询、HTTP链路、事务代理与回滚通过 |
| 真实 Redis | TTL、JSON、锁互斥、非所有者解锁拒绝、新所有者保护通过 |
| Docker Compose开发环境/app配置 | config --quiet通过 |
| 生产配置缺失检查 | APP_IMAGE留空时明确失败，符合必填契约 |
| 模板复制 | 临时复制、重命名包/artifact、与shared组成验证reactor，verify通过 |
| Docker镜像 | bootstrap上下文构建成功 |
| Java21容器运行 | overall health/liveness/readiness全部UP，POST201，GET查询匹配 |
| prod profile | 容器确认为prod；健康200，Sample API/OpenAPI/Swagger均403 |

验证环境：宿主机 Microsoft JDK25.0.4、编译release21、Maven Wrapper3.9.16；容器 Temurin21.0.12.1、PostgreSQL17.11、Redis7.4.11。

集成测试命令（端口来自本轮隔离测试环境）：

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:17432/project \
SPRING_DATA_REDIS_PORT=17379 \
./mvnw -B -ntp -Pintegration verify
```

本轮的隔离 Compose 项目 cola-scaffold-verification-20261007 已停止，临时容器、网络、测试持久卷和模板复制件已清理。验证镜像 cola-scaffold-verification:local 保留在本地，未发布。可执行 JAR 位于 project-bootstrap/target/project-bootstrap-0.1.0-SNAPSHOT.jar。

## 继续使用

从根 README 的开发步骤启动 PostgreSQL/Redis，再以 --spring.profiles.active=dev 启动 JAR。新增领域按 templates/domain-module/README.md 操作，在根 POM、bootstrap POM 与组合根注册。

每个业务领域只有一个 Maven模块，技术依赖在编译路径上对所有内部包可见，因此包级隔离由 ArchUnit强制检查。生产认证授权、pgvector扩展、具体AI业务与强一致分布式锁协议没有在本轮预设；RedisLock是没有续租/fencing的短期技术租约。
