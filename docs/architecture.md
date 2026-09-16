# ToLink 模块化单体架构

## 当前工程

ToLink 使用一个 Maven reactor 构建一个单体运行时。模块之间有编译期边界，但最终由一个 Spring Boot JAR 启动：

```text
tolink/                         # 聚合父 POM，packaging=pom
├── shared/                     # 纯 Java、稳定的跨领域契约
├── bootstrap/                  # 唯一可执行模块和全局运行配置
├── agent/                      # 当前唯一业务领域模块
└── templates/
    └── domain-module/          # 可复制模板，不参与默认构建
```

未来领域模块使用根目录 `<domain>`，一个领域对应一个 JAR，包层固定为：

```text
org.xaspire.tolink.<domain>
├── adapter          # HTTP、消息等入站适配
├── application      # 用例、port.in、port.out、窄 api
├── domain           # 实体、值对象、领域服务、领域事件
└── infrastructure   # Repository、Mapper/PO、Gateway、厂商适配
```

当前只实例化 `agent` 领域的空骨架；`identity`、`far`、`membership`、`persona`、`recommendation`、`workflow` 和 `connection` 都在未来按模板按需加入。

## 依赖规则

```text
domain         -> shared（仅在确有稳定公共契约时）
application    -> domain、shared
adapter        -> application、shared
infrastructure -> application、domain、shared
bootstrap      -> shared、所有已启用领域
```

领域层不能依赖 Spring、MyBatis-Plus、Redis、OSS、AgentScope、adapter 或 infrastructure。应用层不能反向依赖 adapter/infrastructure；基础设施层不能依赖 adapter。

领域模块默认互相独立。跨领域协作只能通过：

1. 对方明确公开的 `application.api`；
2. 调用方定义的 `application.port.out`；
3. 领域事件或 `shared` 中真正领域无关的稳定契约。

不得引用其他领域的 domain、infrastructure、Mapper、Repository、PO 或内部实现。bootstrap 可以依赖领域，领域不能依赖 bootstrap。

`../tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java` 使用 ArchUnit 检查这些包级边界，并允许当前没有业务领域时规则为空匹配。未来领域加入 reactor 后会自动纳入扫描。

## Bootstrap 责任

bootstrap 保留当前基础设施能力：Spring Boot 启动入口、DataSource、Hikari、MyBatis-Plus、Flyway、Redis、Security、Actuator、springdoc、Jackson/线程池等全局配置，以及 Docker 运行所需的应用资源。

AgentScope 的可选装配位于 agent 的 `infrastructure`；bootstrap 只通过 `tolink-agent` 依赖组装它。当前不创建 platform、common、api、client 或独立 migrations 模块。

安全边界保持默认拒绝：健康端点匿名开放，开发环境可开放 springdoc，其他请求继续 `denyAll`。readiness 检查数据库，不把 Redis 当作业务事实来源。

## Flyway 迁移

Flyway 只在 bootstrap 中初始化一次，所有模块共用一个 PostgreSQL schema 和 `flyway_schema_history`。技术基线 `V1__baseline.sql` 归 bootstrap；领域表结构由领域模块自己的 `src/main/resources/db/migration` 维护，并随领域 JAR 进入单体 classpath。

版本号在整个仓库内单调递增，描述中包含领域名：

```text
V1__baseline.sql
V2__identity_baseline.sql
V3__far_add_profile.sql
```

领域不能重新从 `V1` 开始编号。

## 新增领域

复制 `templates/domain-module` 到根目录 `<domain>` 后：

1. 将 Java 路径、包声明、测试包中的 `template` 替换为领域名；
2. 修改 artifactId 为 `tolink-<domain>`；
3. 在根 POM 注册类似 `<module>identity</module>` 的模块项，并替换为实际领域名；
4. 在 `../tolink-bootstrap/pom.xml` 添加该领域依赖；
5. 将领域专属数据库、缓存和第三方适配依赖放入该领域的 infrastructure；
6. 运行 `./mvnw -pl bootstrap -am verify`，再按需运行 Compose 集成测试。
