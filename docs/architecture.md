# COLA Light 模块化单体

模块依赖为 bootstrap → 业务领域 → shared，bootstrap 可直接依赖 shared。shared 不包含业务服务或领域模型；业务模块不依赖 bootstrap。project-sample 是唯一复制模板，生产代码只定义包职责。

根包为 org.xaspire.project，每个领域占一个一级子包；遵守该命名约定，供架构测试自动识别：

```text
<domain>
├── api/{facade,dto,command,query}
├── adapter/{web,mq,job}
├── application/{service,command,query,assembler}
├── domain
│   ├── model/{aggregate,entity,valobj}
│   ├── service
│   ├── repository
│   ├── port
│   └── event
└── infrastructure
    ├── persistence/{mapper,dataobject,repository}
    ├── gateway
    └── client
```

## 内部方向

adapter → application → domain ← infrastructure。adapter 也可依赖本模块 api.facade，接口由 application 实现。application 管编排、事务、事件发布和 DTO 转换；infrastructure 实现 domain.repository/port，不依赖 application、adapter 或 api。

domain 只依赖本领域 domain 和 java.lang/java.util/java.time/java.math，不引入 Spring、MyBatis、Redis、JPA、HTTP Client或shared技术封装。api 不暴露内部模型或技术类型，可使用 Jakarta Validation 描述请求契约。

层处于一个 Maven模块，编译路径上的技术依赖对全部包可见；ArchUnit 强制保护包级边界。

## 跨领域通信

同步协作只引用对方顶层 api；业务完成后的通知可用事件。domain.event 是内部领域事实，公开消费契约位于 api，由 application 转换/发布。其他模块不得引用 domain.event 来绕过公开边界。

## Composition Root

Application 位于 bootstrap，仅扫描 bootstrap。接口与实现通过 bootstrap 的 @Bean 或明确 @Import 注册；Mapper 由 bootstrap @MapperScan 注册。替换实现只修改组合根。

application 允许 @Service/@Transactional，@Service 是允许的应用层标记，不扩大包扫描，也不允许应用层 @Configuration。infrastructure/shared 不自动声明 @Component/@Repository/@Configuration；domain 禁止所有框架注解。

Spring Boot 在启动模块创建 DataSource、Redis和事务管理器。空白模板没有具体业务 Bean 或装配配置；新增领域能力后，由 bootstrap 显式选择其实现。

## Shared

响应、异常、ID 契约使用纯 Java；JSON/Redis 技术包允许对应框架。业务缓存键与失效策略属于领域 infrastructure。RedisLock 是 SET NX + TTL 租约锁，Lua 比较 token 后删除，不续租、不重入、没有 fencing；调用方每次使用新 token 并在租期内完成工作。

## Flyway

bootstrap 初始化一次 Flyway，扫描实际领域 JAR 的 classpath:db/migration，共享 public.flyway_schema_history 和仓库级唯一递增版本。模板不预设表结构或迁移；实际业务领域按需要创建 schema/SQL。已应用的迁移不能修改或重用版本号。

bootstrap 基础设施测试单独扫描测试资源 db/integration-migration，使用 integration_probe.integration_flyway_schema_history，避免技术探针占用 public 或与业务迁移编号冲突；测试资源不进入应用 JAR。

## Architecture as Code

project-sample 的 ModuleArchitectureTest 随复制带走，扫描本模块生产代码：
- domain 纯净；
- 五层方向；
- api 不依赖技术类型；
- 跨领域只访问 api，禁止访问 bootstrap。

空模板明确允许空层；ModuleArchitectureRulesTest 的正例和反例证明规则可捕获后续代码的违规依赖。测试夹具只属于 test，不打包到生产。

bootstrap 的全局 ArchitectureTest 覆盖全部启用模块，继续检查 shared/业务/bootstrap 方向、模块环与组合根政策；全局反例与业务模板独立，避免依赖演示模型。

## 运行

dev 开放文档、prod关闭文档；健康端点公开，业务访问必须由实际安全配置授权。readiness包含数据库，Redis是技术缓存。启动需显式选择profile。

参考 [COLA官方架构](https://github.com/alibaba/COLA) 和 [ArchUnit分层规则](https://www.archunit.org/userguide/html/000_Index.html#_layer_checks)；架构按本工程的领域零框架、顶层api与唯一组合根契约实施。
