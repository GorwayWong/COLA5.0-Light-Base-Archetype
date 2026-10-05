# ToLink 团队开发骨架审查：DDD、COLA 5.0 Light、模块化单体

日期：2026-10-04。基线：`47da1c4` 与本次工作区。本文扩展[前次分层审查](/Users/gorwaywong/Projects/codeup/tolink/plans/cola-light-architecture-review.md)。本次通过用户指定的 Exa、Context7、GitHub 追加官方与开源源码核验，新增结论见第 8–11 节；没有修改源码、构建文件或 AGENTS.md。本次未重复执行第 7 节已有的测试，也未构建外部样例。

**可以作为团队骨架的基础，但目前不宜直接宣布为可交付的团队标准模板。架构方向合理，主要缺口是交付入口失效、边界规则不完整、缺少仓库内的团队接入和持续验证流程。没有必要推倒重建，也不需要通过增加基础模块来解决这些问题。**

本次来源核验没有确认一个同时满足“官方 COLA Light 5.0 来源、每业务上下文一个 Maven JAR、单一启动模块、可证实生产成熟度”的外部范本。最可靠的组合是：官方 Light 确定层职责，已公开的模块化 DDD 样例提供可执行边界，社区脚手架提供模板与交付验证做法。不要将 COLA 5 组件版本、Light 风格单模块工程或定制衍生框架等同于精确匹配案例。

## 1. 网络资料给出的依据

以下区分资料内容与对本项目的建议；没有一份官方资料定义“DDD + COLA Light 单体多模块”的唯一标准目录。

| 资料 | 可用于本项目的原则 |
| --- | --- |
| [COLA v5.0.0 官方说明](https://github.com/alibaba/COLA/blob/v5.0.0/README.md)、[Light 模板 POM](https://github.com/alibaba/COLA/blob/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources/pom.xml)、[5.0.0 发布模板](https://repo.maven.apache.org/maven2/com/alibaba/cola/cola-archetype-light/5.0.0/cola-archetype-light-5.0.0.jar) | COLA 架构与组件应分别看待；Light 采用一个项目内的 adapter/application/domain/infrastructure 分包，不能把 service 模板的模块清单强加给 Light |
| [Martin Fowler：Bounded Context](https://martinfowler.com/bliki/BoundedContext.html) | 限界上下文包含明确的模型语义和上下文关系，目录或 JAR 本身不足以证明边界正确 |
| [Martin Fowler：Monolith First](https://martinfowler.com/bliki/MonolithFirst.html) | 先在单体中发现和稳定边界，是一种有依据的起步策略；这不意味着可以放弃模块隔离 |
| [Alistair Cockburn：Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture) | 内部应用通过端口隔离外部技术，适配器可以替换为测试实现；不应让业务模型依赖数据库或厂商 SDK |
| [Spring Modulith：模块模型](https://docs.spring.io/spring-modulith/reference/fundamentals.html)、[结构验证](https://docs.spring.io/spring-modulith/reference/verification.html) | 模块应有公开契约和内部实现；验证应覆盖无环、只访问公开接口，以及可选的显式允许依赖 |
| [Spring Modulith：模块测试](https://docs.spring.io/spring-modulith/reference/testing.html) | 支持单模块及限定依赖的集成测试；一个模块需要大量其他模块的 Bean 才能测试，是值得检查的耦合信号 |
| [Flyway：Versioned migrations](https://documentation.red-gate.com/fd/versioned-migrations-273973333.html) | 迁移版本必须唯一；已进入永久下游环境的迁移应保持不变，通过新迁移前进；时间戳可以减少并行编号冲突 |
| [Spring Modulith：应用事件](https://docs.spring.io/spring-modulith/reference/events.html) | 同步事件影响事务边界；提交后异步事件存在失败或丢失风险，可靠发布需要相应保障 |

Spring Modulith 在这里是模块治理的参考，并非建议直接安装其当前版本。项目基线为 Spring Boot 3.5；如以后选用该工具，应单独核对兼容版本，不能照抄当前文档的依赖版本。

## 2. 每个模块能否作为团队骨架

| 模块 | 判定 | 建议 |
| --- | --- | --- |
| 根 `tolink` | 合适 | 保持 reactor 与版本管理职责；增加持续验证入口，避免用父 POM 向所有模块注入技术依赖 |
| `tolink-agent` | 合适的四层骨架，尚非已验证的业务上下文 | 保留；用一个真实用例明确业务语言、规则与端口；不要把 AgentScope SDK 封装本身当作完整领域建模 |
| `tolink-shared` | 当前很轻，保留或按需创建均可 | 当前只有包说明与无依赖 POM，不值得优先删除；限制新增内容，防止变成 common/utils 杂物模块 |
| `tolink-bootstrap` | 合适的装配根 | 保留全局启动、连接、安全、迁移装配；具体业务 Mapper、Repository 实现和用例归所属领域 |
| `templates/domain-module` | 接近可用，但接入步骤未闭环 | 修复复制后的父路径和命名约定，再验证能接入 reactor、bootstrap 与架构测试 |

证据：[根 POM](/Users/gorwaywong/Projects/codeup/tolink/pom.xml:21)、[agent POM](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/pom.xml:18)、[shared POM](/Users/gorwaywong/Projects/codeup/tolink/tolink-shared/pom.xml:14)、[bootstrap POM](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/pom.xml:18)、[模板 POM](/Users/gorwaywong/Projects/codeup/tolink/templates/domain-module/pom.xml:7)。

模块依赖保持 `bootstrap → agent → shared`，业务上下文内部保持四层包即可。shared 和 bootstrap 是支持模块，不需要各自制造四层目录，也不应强制每个业务上下文再拆成四个 Maven 项目。

## 3. 团队开始共同开发前，应补齐的内容

P0 表示当前入口不可用；P1 表示作为团队统一骨架应优先补齐。努力为 S/M/L，风险指修复本身的风险。

| 优先级 | 项目现状及影响 | 最小补齐方式 | 努力 / 风险 / 置信度 |
| --- | --- | --- | --- |
| P0 | Dockerfile 复制不存在的 shared/agent/bootstrap 目录，打包命令和 JAR 路径也使用旧目录；现有容器入口不能构建当前工程 | 对齐三个 tolink-* 目录、`-pl tolink-bootstrap`、输出 JAR 路径；同步集成 Compose | S / 低 / 高 |
| P1 | ArchUnit 尚未完整表达文档依赖矩阵，存在 domain→application、领域→bootstrap、shared→领域应用等缺口 | 补全方向约束；增加“故意违规应失败”的规则测试，避免仅在空层上通过 | M / 低 / 高 |
| P1 | 四层外的业务包及未识别的跨领域目标会绕开现有检查；公开 API 的类型边界也未单独限定 | 验证业务包归层；限定 application.api 的契约类型；按实际协作声明允许依赖关系 | M / 低 / 高 |
| P1 | 模板复制到根目录后仍使用 ../../pom.xml，接入文档也存在旧目录与构建命令 | 明确模板原位置与复制后父路径的区别；统一实际目录名，验证新模块接入完整步骤 | S / 低 / 高 |
| P1 | 仓库内未发现 CI 定义；测试是否作为合并门禁没有本地证据 | 在团队实际使用的平台纳管架构测试和构建；数据库相关变更执行集成验证 | M / 低 / 中：远端 CI 可能另有配置 |
| P1 | 无根 README/CONTRIBUTING；现有开发说明有命名漂移，新成员无法直接依赖一个可靠入口 | 根文档写清环境、启动、测试、集成验证、新模块接入；团队选定并固定格式检查 | S / 低 / 高 |
| P1 | 文档声明 agent 为限界上下文，但未记录业务术语、上下文关系、团队所有权和数据访问边界 | 为实际开始开发的上下文记录职责、关键术语、公开 API、表所有权、依赖与负责人；不提前为未来领域写完整模型 | M / 低 / 高 |

### 3.1 容器交付入口：已确认的实际阻断

本地路径检查结果为：`shared`、`agent`、`bootstrap` 均不存在，`tolink-shared`、`tolink-agent`、`tolink-bootstrap` 均存在。

Dockerfile 的 `COPY` 在 Maven 执行前就会失败；即使仅修正 COPY，旧的 `-pl bootstrap` 和 `/workspace/bootstrap/target/...` 仍不适配当前工程。集成 Compose 也覆盖了旧的模块选择命令。[Dockerfile](/Users/gorwaywong/Projects/codeup/tolink/Dockerfile:9)、[打包与测试命令](/Users/gorwaywong/Projects/codeup/tolink/Dockerfile:13)、[JAR 路径](/Users/gorwaywong/Projects/codeup/tolink/Dockerfile:28)、[集成 Compose](/Users/gorwaywong/Projects/codeup/tolink/compose.integration.yaml:5)

这是前次仅关注包分层审查未覆盖的交付问题，应优先于新增技术能力。

### 3.2 架构约束：需要从约定变成可执行规则

当前已有无环、跨领域只能访问 application.api、adapter 不访问 domain/infrastructure 等规则，值得保留。缺口包括：

- domain 的禁止列表漏掉 application，三份测试均如此。[全局规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:23)、[agent 规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/src/test/java/org/xaspire/tolink/agent/architecture/AgentArchitectureTest.java:14)
- shared 未禁止依赖 application、adapter、bootstrap；领域→bootstrap 也未显式禁止。[shared 规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:70)、[排除根包](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:126)
- 未识别为四层的目标被跳过，领域根包或 service 等额外包可能逃过检查。[选择器](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:88)、[跳过判断](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:108)
- 同领域的 application.api 可以引用自己 domain 的类型，现有规则不阻止将实体或聚合作为公共 API 的入参/返回值。当前没有实际 API 泄漏，建议在第一个跨领域契约出现时以规则和契约测试保护。

模块可公开的契约与允许依赖图是两个不同约束：只调用公开 API，不代表任意两个模块都应该建立业务依赖。这一补充参考 [Spring Modulith 的验证模型](https://docs.spring.io/spring-modulith/reference/verification.html)，可直接用现有 ArchUnit 实现，暂不必更换框架。

### 3.3 DDD 协作契约：缺少约定，不是缺少空目录

当前 architecture.md 有层职责，没有给实际领域记录统一语言、上下文关系和数据所有权。[领域说明](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:26)、[跨领域规则](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:40)

建议团队至少对以下问题形成一份短文档，并在真实用例出现时更新：

| 决策 | 最小约定 |
| --- | --- |
| 领域与模块 | 一个模块承载内聚业务能力；不能仅因第三方 SDK、Controller 或数据库表不同就拆上下文 |
| 端口归属 | domain 需要的 Repository/Gateway 接口归 domain；应用编排需要的出站接口归 application；实现归 infrastructure |
| DTO 边界 | HTTP 请求/响应归 adapter；用例契约归 application；跨模块公开类型归 application.api；按实际差异转换，不强制每层制造一套重复 DTO |
| 数据所有权 | 每张业务表有所属模块；其他模块不直接写表、不复用其 Mapper/PO；跨领域读取与报表需求单独明确规则 |
| 事务 | 在用例层明确事务边界；同步跨领域调用是否共同提交由业务决定，不强制引入分布式事务或一律 REQUIRES_NEW |
| 外部系统 | SDK 和外部领域类型在适配器处转换，不直接进入领域模型 |
| 团队责任 | 每个实际模块有维护负责人；公开契约变更需调用方参与评审；具体可使用团队平台的代码所有权机制 |

这些是针对本项目的建议，不是 COLA 的强制包名规范。依据包括 [DDD 限界上下文](https://martinfowler.com/bliki/BoundedContext.html) 与 [端口适配器原则](https://alistair.cockburn.us/hexagonal-architecture)。

## 4. 有用但可以随业务补齐的内容

| 内容 | 当前判断与触发时机 |
| --- | --- |
| 一个真实的纵向用例 | 第一个实际需求时形成 adapter→application→domain，并以 infrastructure 实现端口；加入领域不变量、用例和集成测试。不要先提交永久保留的 Account/Charge 演示代码 |
| 异常与错误码、请求校验、分页 | 第一个 HTTP API 前约定最小错误响应和校验归属；无业务接口时，不需要预造完整公共响应框架 |
| 模块独立测试 | 实际业务出现后，应能用端口替身测试领域/用例，限定模块范围做集成测试；无需提前为所有未来模块创建测试基础类 |
| SQL 并行协作规则 | 保留当前仓库统一版本策略，补重复版本检查、合并前冲突处理和禁止改已发布 SQL 的规则；团队并发增多时再评估时间戳编号 |
| 可观测性 | HTTP/异步任务出现后增加关联标识和关键结构化日志；当前 Actuator 健康检查已具备，无需先引入完整遥测平台 |
| 鉴权 | 当前默认 denyAll 是安全起点；真实身份和授权规则随业务落地，不应因为骨架阶段未实现 identity 就判为架构缺失 |
| 可靠领域事件 | 发生不可丢失的提交后异步协作时，明确持久化、重试、幂等、事件兼容策略；此时才评估 outbox/发布日志，不提前引入消息中间件 |

模块测试建议参考 [Spring Modulith 测试文档](https://docs.spring.io/spring-modulith/reference/testing.html)。事件可靠性建议参考 [Spring Modulith 事件文档](https://docs.spring.io/spring-modulith/reference/events.html)；普通 Spring 事件并不天然等于可靠异步消息。

项目已选单库单 schema、全仓库递增迁移版本，这可以用于模块化单体；不能把“每个模块必须独立数据库/schema”当作 DDD 要求。需要补的是业务表所有权和团队并行迁移流程。[项目 Flyway 决策](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:58)

Flyway 官方支持递增数字，也说明时间戳有助于减少并行版本冲突；因此没有证据要求现在推翻整数编号。无论采用哪种编号，版本都必须唯一，已应用的迁移应通过新脚本演进。[Flyway 官方规则](https://documentation.red-gate.com/fd/versioned-migrations-273973333.html)

## 5. 冗余、过早引入与值得保留的内容

目前只有 13 个生产 Java 文件，多数业务层是包说明，源码并不臃肿。需要控制的是技术选择和规则维护成本。

| 项目内容 | 判断 | 建议 |
| --- | --- | --- |
| shared 空 JAR | 轻量预留，有少量维护成本；不构成优先级高的冗余 | 对 ToLink 可保留；若制作跨项目通用模板，可等第二个上下文确有公共契约时再加入 |
| 全局、agent、复制模板三处层规则 | 规则内容重复，但承担 reactor 总检查、模块局部反馈和新模块起步三种用途；不能直接删为只剩一处 | 固定一份规则规范并增加一致性验证；规则成熟后再评估小规模复用，不先新增测试平台模块 |
| agent 的 Spring 与 AgentScope 依赖 | ToLink 技术基线明确选择，属于领域技术适配，不是已确认冗余 | 留在 infrastructure；若导出通用骨架，做成可选内容，不传播到其他领域或 shared |
| Redis | 当前无业务缓存，仅有连接、集成测试，compose 仍要求 Redis 就绪 | 对 ToLink 基线可保留；通用骨架可按需启用。readiness 不依赖 Redis，但 compose 启动仍依赖它，需团队明确这项运行要求 |
| OSS 属性和开关 | SDK/业务能力尚未落地，只是预留配置，成本小 | 不扩展为 storage 平台；可随首个存储需求落实。不是必须立刻删除的缺陷 |
| MyBatis-Plus、Flyway、数据库探针 | 已有真实基础设施验证用途 | 保留；具体业务持久化代码归领域 infrastructure |
| `V1__baseline.sql` 仅 SELECT 1 | 技术迁移与启动验证基线，无业务表 | 无需为“整洁”删除；已有共享环境执行过时尤其不应修改历史迁移 |
| package-info 与 domain-module 模板 | 轻量表达职责、为团队提供统一入口 | 保留并修正模板接入步骤 |

证据：[可选 AgentScope 装配](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/src/main/java/org/xaspire/tolink/agent/infrastructure/agentscope/AgentScopeConfiguration.java:10)、[Redis 启动依赖](/Users/gorwaywong/Projects/codeup/tolink/compose.yaml:19)、[readiness](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/main/resources/application.yml:43)、[OSS 属性](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/main/java/org/xaspire/tolink/bootstrap/configuration/OssProperties.java:5)、[技术迁移](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/main/resources/db/migration/V1__baseline.sql:1)。

暂时不要增加独立 common/platform/client 模块、每层 Maven 子模块、通用 BaseEntity/BaseService/BaseRepository、事件总线、分布式事务、CQRS 框架、代码生成平台。它们都需要明确需求和复用证据；COLA Light 分层本身不要求这些设施。

## 6. 已具备的团队起步能力

已有 Maven Wrapper、Java 21 基线、单一 Spring Boot 可执行入口、开发/生产/集成配置、环境变量样例、容器编排定义、默认拒绝安全策略、Actuator 存活与数据库就绪检查，以及真实 PostgreSQL/Redis 集成测试。

`InfrastructureIT` 覆盖启动与 Flyway、MyBatis 插入查询、JSONB、事务回滚、Redis TTL，不应为了换一种测试方式全部推翻。[集成测试](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/bootstrap/InfrastructureIT.java:76)

仍需注意：集成测试使用 `WebEnvironment.NONE`，不验证 HTTP 安全行为；第一次添加公开/受保护业务接口时，应增加相应 HTTP 测试。这是业务接入触发项，不要求当前建立完整接口测试平台。

## 7. 验证结果与建议验收顺序

本次执行了离线、定向的现有架构测试：

```bash
rtk proxy ./mvnw -B -o -pl tolink-bootstrap -am \
  -Dtest=ArchitectureTest,AgentArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

结果：`BUILD SUCCESS`；agent 4 条、bootstrap 7 条规则，共 11 条，零失败。当前空业务层和未覆盖方向不会因这些测试通过而自动获得保证。

未运行 Docker 构建、PostgreSQL/Redis 集成测试或完整 verify；Docker 路径阻断由文件路径和构建脚本直接确认。技术基线记录的过去集成成功不代表当前目录改名后的容器入口仍有效。

建议按以下顺序验收团队骨架：

1. 修复命名漂移与模板父路径；证明根构建、容器打包和模板接入步骤可执行。
2. 补全架构规则与违规样例；将架构测试和构建纳入实际 CI/合并门禁。
3. 写一份短的团队入口文档和上下文契约；明确表所有权、公开接口、事务与迁移流程。
4. 以第一个真实用例验证端口替换、领域规则和模块测试，再按业务选择错误响应、鉴权和事件可靠性设施。

完整基础构建命令应使用实际模块名 `rtk proxy ./mvnw -B -pl tolink-bootstrap -am verify`。集成验证还需独立 PostgreSQL/Redis 环境和 integration profile；容器路径修正后再验收对应 Compose 流程。

审查限制：本次未做完整安全或性能审计，也未验证所有依赖版本和容器镜像的可获得性。未在仓库内发现 CI、根 README、CONTRIBUTING、CODEOWNERS、格式检查配置或领域词汇/上下文映射文档；不排除团队平台另有配置。文中“必须补齐”指作为团队统一骨架的工程判断，并非 COLA 官方认证清单。

## 8. 本次官方与开源样例核验

使用 Exa 执行 5 次查询，共返回 50 条结果、28 个原始 URL；通过 GitHub 进一步检查 POM、目录树、测试和 CI。重复、转载、仅有架构关键词及技能市场页面不作为实践证据。Context7 用于定位 COLA、ArchUnit 和 Spring Modulith 文档，再回到原始源码核对。

| 样例 | 核验结论 | 可学习内容 | 不应据此推导的结论 |
| --- | --- | --- | --- |
| [Alibaba COLA Light v5.0.0](https://github.com/alibaba/COLA/tree/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources) | 精确版本官方基准；单项目包级四层，计费教学场景 | 应用服务编排、领域行为、domain.gateway 接口、infra 实现、统一语言 | 官方未提供“每上下文一个 JAR”的团队骨架标准，也不能据此证明 ToLink 的整个交付流程可用 |
| [ddd-rbac-scaffold-lite](https://github.com/yiyongbo/ddd-rbac-scaffold-lite/tree/3b2064268d8176482c323ff89f621da081422f0f) | README 声明 COLA 5 Light 风格；POM 与结构文档确认只有一个 Spring Boot 模块 | 内层接口、外层实现；请求、用例 DTO、领域对象、PO 的分工；小团队接入文档 | 不能证明由官方 5.0.0 archetype 生成，不能充当领域多 JAR 或生产成熟度证据 |
| [Egon-COLA](https://github.com/AllenDEricDAlexander/Egon-COLA/tree/f0f563bf94c92834890411083fb53756bff55e90) | 定制衍生框架；Light source POM 为一个 JAR，使用自身 5.4.1 版本和额外组件体系 | 生成工程检查、源码/说明分开核验、CI verify、明确组件归属 | 不是原版 Light 5.0；名为 large-monolith 的文档当前也声明单模块，不能按文件名认定为多领域 JAR |
| [Midgard backend template](https://github.com/Yggdrasil-Labs/midgard-backend-template) | 根 POM 管理 COLA components 5.0.0，但拆 client/adapter/app/domain/infrastructure/start 六个技术层模块；README 定位为微服务模板 | 领域实体与持久化对象分离、接口转换的局部做法 | 不是本项目要求的 Light 包级分层，也不是按上下文分 JAR 的单体；不能搬入其注册/RPC/平台依赖 |
| [ddd-by-examples/library](https://github.com/ddd-by-examples/library/tree/5225ff7a0f0c1e0751b91cdd64f925cd001d7555) | 系统化 DDD 教学样例；上下文按包隔离的模块化单体，不使用 COLA | 模块依赖测试、领域与应用边界、公共包隔离、业务模型与测试的联系 | 只能作为治理和建模参考，不是 COLA 5 Light 精确样例；不沿用其旧 Java/JUnit/依赖版本 |

“有源码和 CI”不等于“已证明生产成熟”。本次没有运行这些外部项目，也没有其线上规模、可靠性或团队实践的独立证据；采用可核验的局部做法，不整体复制。

### 官方材料需要按版本与实际代码读取

- v5.0.0 标签的根 README 仍保留部分旧版本说明，而当前 master 的 README 已增加 5.0 发布说明。精确版本判断以该标签下的 Light 模板为准，而非搜索摘要。[标签模板](https://github.com/alibaba/COLA/tree/v5.0.0/cola-archetypes/cola-archetype-light)、[当前官方说明](https://github.com/alibaba/COLA)
- Light 的 `CleanArchTest` 中规则体仍被注释。Context7 返回的片段展示了规则内容，但不能据片段认定它在 5.0.0 模板中实际执行。[原始测试源码](https://github.com/alibaba/COLA/blob/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources/src/test/java/CleanArchTest.java)
- 官方 domain 的 `AccountDomainService` 使用 Spring Component 与 Jakarta Resource；ToLink 的纯 Java domain 是更严格的团队约束，建议保留，但不要称为官方绝对禁令。[官方领域服务](https://github.com/alibaba/COLA/blob/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources/src/main/java/domain/account/AccountDomainService.java)
- 官方 Light POM 仍引用 `cola-component-test-container:4.4.0-SNAPSHOT`。ToLink 已有独立验证技术栈，无需为了“更符合官方”重新引入这一依赖，也不需要照搬模板的数据库或 Web 技术选择。[官方模板 POM](https://github.com/alibaba/COLA/blob/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources/pom.xml)

## 9. 来自源码证据的最小约束清单

### 9.1 层规则按上下文第一层包识别

保留 `org.xaspire.tolink.<context>.<layer>`。只把紧跟上下文的 adapter/application/domain/infrastructure 视为顶层职责；当前广泛的 `..adapter..` 会误判嵌套同名包，`DomainLayer.from` 又会跳过未知层。补包归属检查，既避免误报，也避免漏检。[当前匹配](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:25)、[层解析](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:130)

依赖矩阵应完整表达当前团队约定，至少阻止 domain→application、任意业务层→bootstrap、shared→任意业务实现。`library` 的测试明确禁止 model→application，并分别禁止 commons→catalogue/lending，可直接作为这些方向的验证参考。[领域边界测试](https://github.com/ddd-by-examples/library/blob/5225ff7a0f0c1e0751b91cdd64f925cd001d7555/src/test/groovy/io/pillopl/library/lending/architecture/LendingHexagonalArchitectureTest.java)、[模块与公共包测试](https://github.com/ddd-by-examples/library/blob/5225ff7a0f0c1e0751b91cdd64f925cd001d7555/src/test/groovy/io/pillopl/library/ModularArchitectureTest.java)

领域技术禁用名单也应覆盖实际采用的 ORM/SDK 类型，而非只禁止某个 starter 的包名；例如 Mapper 注解与 MyBatis-Plus 是不同来源。当前没有业务领域类违规，这属于提前完善约束。

### 9.2 公开契约与允许依赖分别检查

跨上下文只访问对方 application.api，还需说明“哪些上下文允许互相依赖”。不必现在引入注解框架；一份小的明确依赖表加 ArchUnit 即可。API 的参数、返回值、泛型、异常和事件载荷不暴露 domain 实体、PO、Mapper、Spring Web 或厂商 SDK 类型。

这比仅检查 import 的入口路径更完整。Spring Modulith 的模型分别检查无环、公开 API 和可选允许依赖列表，值得借鉴，但不要求现在安装该框架。[官方验证规则](https://docs.spring.io/spring-modulith/reference/verification.html)

应用接口可以承担入站用例契约；不要机械复制 application.api、port.in、Facade 三套相同接口。domain 所需接口归 domain.gateway，用例特有的外部能力归 application.port.out，技术实现归 infrastructure。编排属于所属上下文的 application，bootstrap 不参与业务流程。

### 9.3 规则自身必须有违规样例

为规则准备合法与故意违规的测试类，通过独立导入样例验证其确实会失败。优先验证 domain→application、shared→业务层、未知包绕过、跨领域内部类型引用、循环依赖。避免用“当前空骨架全部通过”代替规则有效性的证明。

ArchUnit 官方提供故意破坏架构的示例代码用于测试规则；其文档也说明允许空匹配会跳过默认的空规则失败保护。[官方示例](https://github.com/TNG/ArchUnit/tree/main/archunit-example)、[空规则说明](https://www.archunit.org/userguide/html/000_Index.html#_empty_should)

当前骨架可以允许未落地的层为空，但应另外验证启用上下文是否被纳入扫描。仅加入 Maven reactor 不代表类会出现在 bootstrap 测试 classpath；必须同步加入 bootstrap 依赖，才会被其 `@AnalyzeClasses` 导入。[扫描入口](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:19)

### 9.4 模板接入是一个需要验证的工程契约

模板验收至少覆盖：复制后的 parent relativePath、artifactId/包名替换、根 reactor 注册、bootstrap 装配依赖、架构扫描覆盖、资源迁移加载。按实际业务模块目录固定命名，不能在 docs、Docker、POM 中各用一套。

Egon-COLA 提供生成工程 verifier，检查文件、配置、包装及源码约束；其 CI 执行 `clean verify`。值得借鉴的是模板检查与持续构建，而不是其大量自定义组件和几百行断言。[生成工程 verifier](https://github.com/AllenDEricDAlexander/Egon-COLA/blob/f0f563bf94c92834890411083fb53756bff55e90/egon-cola-archetypes/definitions/egon-cola-archetype-light/src/test/resources/projects/basic/verify.groovy)、[CI](https://github.com/AllenDEricDAlexander/Egon-COLA/blob/f0f563bf94c92834890411083fb53756bff55e90/.github/workflows/ci-backend.yml)

ToLink 不需要先发布自己的 Maven archetype；一个小的接入验证即可。静态 verifier、Maven 构建、Boot 启动和数据库集成是不同证据，不能互相替代。

### 9.5 团队约定应短、明确并可评审

每个实际上下文记录业务职责、关键术语、公开 API、数据表所有权、允许依赖和负责人；全局记录端口、DTO、事务、迁移和错误响应归属。官方 Light 的计费示例从业务问题、统一语言及周边系统关系出发，而非先生成大量 model 子目录。[官方示例说明](https://github.com/alibaba/COLA/blob/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources/README.md)

表与 Mapper/PO 归所属领域 infrastructure；共享数据库不等于任意跨领域写表。用例层明确事务边界，领域维护不变量，技术层实现并发/持久化机制；跨领域事务和异步一致性按真实业务决定，不将一个技术方案强制到所有用例。

领域 Repository 围绕聚合或业务能力，不要求每张表一套 Repository/DomainService。简单查询可以由应用查询端口获取所需投影，接口归 application、实现归 infrastructure；不要为了返回 DTO 让 domain 引用 application，也不因“CQRS”强制引入总线或另一个数据库。

### 9.6 团队验证入口应真正执行

合并门禁至少执行固定 Java/Maven 基线下的构建与架构测试；数据库、迁移、运行配置相关变更再执行真实集成检查。修正 Docker 路径后，应验证容器产物能启动，不能只验证 POM 编译。

根 README 给新成员一个准确入口，格式规则由团队选定并检查。先使用当前 Maven/ArchUnit 工具，不同时新增多个静态分析或架构框架。版本管理集中于父 POM，技术依赖由使用它的业务模块声明，不放进 shared 或全局父 dependencies 传播。

## 10. 推荐优先级与验收条件

| 顺序 | 完善项 | 验收条件 | 努力 / 风险 / 置信度 |
| --- | --- | --- | --- |
| 1，P0 | Docker、Compose、文档和模块目录命名一致；模板复制后父路径正确 | 根构建、容器打包、模板接入用同一套实际模块名完成 | S / 低 / 高 |
| 2，P1 | 完整层依赖、shared 独立、未知包归层、跨领域契约和依赖图 | 故意违规样例触发失败；合法样例通过；所有启用模块纳入扫描 | M / 低 / 高 |
| 3，P1 | 最小 CI、根开发文档、模板接入验证 | 团队实际平台将构建与架构测试设为合并门禁；新成员可复现启动/测试流程 | M / 低 / 中：远端门禁未核验 |
| 4，P1 | 上下文与数据所有权、端口/DTO/事务规范 | 真实开始开发的上下文有短契约文档；第一个真实用例的各层落位可清晰解释 | S–M / 低 / 高 |
| 5，随业务 | 错误响应与校验、HTTP 安全测试、并发控制、可靠事件与日志关联 | 在对应业务出现时补契约与测试，不提前制造框架 | 按业务 / 中 / 高 |

第 1 项的源码证据位于 [Dockerfile](/Users/gorwaywong/Projects/codeup/tolink/Dockerfile:9)、[集成 Compose](/Users/gorwaywong/Projects/codeup/tolink/compose.integration.yaml:5)、[模板 POM](/Users/gorwaywong/Projects/codeup/tolink/templates/domain-module/pom.xml:11)。第 2 项的直接缺口见 [domain 规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:23)、[shared 规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:70)、[跨领域检查](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:108)。

## 11. 为了简洁，应保留与暂不采用的内容

保留每个业务上下文内部四层、唯一 bootstrap、很小的 shared、当前真实数据库/Redis 集成测试、按需模板和领域专属 AgentScope 适配。domain 无 Spring/SDK 作为团队加严选择继续执行。

不增加全局 application/case/domain/infrastructure JAR，不复制另一框架的 common/facade/starter 七层，不提前引入 Nacos、Dubbo、ShardingSphere、自研 DDL runner、全套 BaseEntity/BaseRepository、自动代码生成、CQRS 框架或 Outbox。社区工程中的技术选择不是 COLA Light 要求，尤其不能因某衍生工程禁用 Flyway 就推翻 ToLink 已明确的 Flyway 决策。

当前核心代码规模很小，没有证据说明必须马上抽公共测试模块或创建平台模块。三处架构规则确有维护重复，可先用同一规范和违规样例控制漂移；只有复用范围稳定后再评估抽取。把更多包和组件加进骨架，不能替代可执行的约束。
