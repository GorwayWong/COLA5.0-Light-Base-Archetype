# 技术基线

| 组件 | 基线 | 用途 |
| --- | --- | --- |
| COLA Light | 5.0 架构思想 | 领域模块内包分层 |
| Java | 21 | 编译与容器目标 |
| Spring Boot | 3.5.16 | 保留原项目父 POM |
| MyBatis-Plus | 3.5.17 | Boot 3 starter |
| Flyway | 11.20.3 | core + flyway-database-postgresql |
| PostgreSQL | 17 | postgres:17-bookworm |
| Redis | 7 | redis:7-bookworm |
| ArchUnit | 1.5.0 | 全局规则与反例 |
| springdoc | 2.8.17 | 开发文档 |
| Maven Wrapper | 3.9.16 | 保留原 Wrapper |

镜像标签限定主版本，补丁随标签更新；生产可将 image 固定到审查过的 digest。未安装 pgvector 扩展或定义向量表。没有预设 AI SDK、OSS、LLM provider、认证业务或 CI 平台。

参考：
- [COLA 官方文档](https://github.com/alibaba/COLA)
- [MyBatis-Plus Quick Start](https://baomidou.com/en/getting-started/)
- [MyBatis-Plus 配置](https://baomidou.com/en/reference/)：多模块 XML 使用 classpath*。
- [MyBatis-Plus TypeHandler](https://baomidou.com/en/guides/type-handler/)：业务需要自定义持久化类型时，在 infrastructure 实现并由 bootstrap 注册。
- [ArchUnit用户指南](https://www.archunit.org/userguide/html/000_Index.html)：空层与分层依赖检查。
- 用户提供的小傅哥骨架 ZIP：参考环境/应用 Compose 分离、应用模块 Dockerfile 和配置组织；不继承 Java 8、Boot 2、MySQL 或部署脚本。

领域模板改造与验证见 sample-module-implementation-report.md。implementation-report.md 和 plans/ 保留历史审查；当前契约以领域模板 README、architecture.md 和 dev-ops 文档为准。

本轮真实验证使用 PostgreSQL 17.11、Redis 7.4.11 和 Temurin Java 21.0.12.1；宿主机测试使用 Java 25.0.4，Maven release 为 21。主版本镜像标签可能使后续运行获取不同补丁版本。
