# COLA 5.0 Light 领域模块模板

project-sample 是可复制的空白领域模块，只提供目录、package 职责和架构测试。src/main/java 只包含 package-info.java，不预设 Controller、Entity、Repository、业务用例或数据库迁移。本仓库交付复制模板，没有 Maven Archetype 发布包。

## 结构

```text
project-sample
├── pom.xml
├── README.md
└── src
    ├── main/java/org/xaspire/project/sample
    │   ├── api
    │   │   ├── facade
    │   │   ├── dto
    │   │   ├── command
    │   │   └── query
    │   ├── adapter
    │   │   ├── web
    │   │   ├── mq
    │   │   └── job
    │   ├── application
    │   │   ├── service
    │   │   ├── command
    │   │   ├── query
    │   │   └── assembler
    │   ├── domain
    │   │   ├── model
    │   │   │   ├── aggregate
    │   │   │   ├── entity
    │   │   │   └── valobj
    │   │   ├── service
    │   │   ├── repository
    │   │   ├── port
    │   │   └── event
    │   └── infrastructure
    │       ├── persistence
    │       │   ├── mapper
    │       │   ├── dataobject
    │       │   └── repository
    │       ├── gateway
    │       └── client
    └── test/java
        ├── org/xaspire/project/sample/architecture
        ├── org/xaspire/project/sample/integration
        └── 架构规则的测试夹具包
```

## 包契约

| 包 | 职责与边界 |
| --- | --- |
| api.facade | 对外用例接口，由 application 实现 |
| api.dto / command / query | 公开输出、写入输入、查询条件；不暴露内部类型 |
| adapter.web / mq / job | HTTP、消息消费者、定时任务入口，转交 application 或 facade |
| application.service | 用例编排、事务边界、领域调用、事件发布 |
| application.command / query | 写/读用例处理器，不直连 Mapper 或技术客户端 |
| application.assembler | DTO 与领域数据转换，不作业务决策 |
| domain.model.aggregate | 聚合根与聚合内一致性 |
| domain.model.entity / valobj | 有身份的实体、按内容比较的值对象及其约束 |
| domain.service | 跨领域对象的业务规则；此处的“跨”指同一业务领域内的多个对象 |
| domain.repository | 聚合存取接口，不暴露 ORM、SQL 或缓存类型 |
| domain.port | LLM、Embedding、工具执行等外部能力的 outbound port |
| domain.event | 本领域已发生的业务事实，不依赖消息中间件 |
| infrastructure.persistence | Mapper、DO、仓储实现及持久化转换 |
| infrastructure.gateway / client | domain port 实现、第三方 SDK 与 HTTP 客户端 |

一个领域对应一个 Maven模块，依赖对模块内全部包可见；包级限制由 ArchUnit检查。domain 只依赖自身 domain 与 JDK核心值类型，禁止 Spring、MyBatis、Redis、JPA、HTTP Client及shared技术封装。

## 用例与装配

依赖为 adapter → application → domain ← infrastructure。adapter 也可以使用本模块 api.facade，由 application 实现。application 可使用 @Service 和 @Transactional，但实现仍由 bootstrap 的 @Bean 或明确的 @Import 注册，启动入口只扫描 bootstrap 包，不扩大扫描到领域模块。

domain 不使用 @Service/@Component/@Autowired/@Transactional；infrastructure 与 shared 不自动声明组件；业务模块不提供 @Configuration。仓储和 outbound port 由 domain 定义，技术实现由 bootstrap 选择。

## 跨模块协作

优先调用另一领域的 api，用于同步能力查询和业务依赖；业务完成后的通知可使用事件。domain.event 仍属于模块内部；给其他模块消费的事件契约应公开在 api，并由 application 映射/发布。其他模块不能为了监听事件而依赖内部 domain.event。

跨模块不得引用 application、domain、adapter 或 infrastructure，也不得依赖 bootstrap。

## 复制为新领域

从仓库根目录，以 agent 为例：

```bash
mkdir project-agent
cp project-sample/pom.xml project-sample/README.md project-agent/
cp -R project-sample/src project-agent/
```

只复制源码和描述文件，排除 target 等构建输出。

1. 将 artifactId project-sample 改为 project-agent，更新模块 README/description；parent relativePath 保持 ../pom.xml。
2. 同时将 main/java 和 test/java 下的 org/xaspire/project/sample 目录改名为 agent；将包声明、测试常量和说明中的 org.xaspire.project.sample 替换为 org.xaspire.project.agent。
3. test/java 的 boundaryfixture 包是测试其他模块边界的夹具，保留即可；它不进入生产 JAR。
4. 根 pom.xml 注册 project-agent；project-bootstrap/pom.xml 增加 org.xaspire:project-agent:${project.version}。
5. 开发公开 api、用例、领域规则与技术实现；bootstrap 新增相应组合配置，按需显式注册 @Bean、@Import、@MapperScan。
6. 数据库迁移由实际业务领域创建在 src/main/resources/db/migration，使用整个仓库唯一递增的 Flyway版本；没有数据库能力的领域无需创建迁移。
7. 执行 ./mvnw -pl project-agent -am clean verify，再执行根 ./mvnw clean verify。

复制完成且包名/模块坐标替换后，空领域即可参与构建。接入业务 Bean、HTTP 入口或 Mapper 时，再补对应 bootstrap 装配；模板没有待解引用的业务类型。

## 测试

从根目录运行 `./mvnw -pl project-sample -am clean verify`，无需数据库或 Redis：
- ModuleArchitectureTest 扫描本模块生产类型，检查领域纯净、内部方向、API 技术类型、跨模块 API。
- 可选层与 allowEmptyShould(true) 允许空白包；添加业务类后规则继续生效。
- ModuleArchitectureRulesTest 通过合法和违规测试夹具证明规则能拒绝层反转、框架泄漏和跨模块内部引用。
- 全局架构测试在 bootstrap 检查所有已注册模块、共享方向、组合根和模块环。

integration 包预留给复制后实际业务的基础设施测试；没有占位业务测试。需要启动整个应用的装配/集成测试放在 bootstrap，避免领域模块依赖 bootstrap。

本仓库 `-Pintegration` 的技术检查使用 bootstrap 自己的测试 Mapper/DO、临时表和 integration_probe schema 内的独立迁移历史表，验证公共基础设施；不会把测试探针打包进应用或占用 public 业务迁移历史。
