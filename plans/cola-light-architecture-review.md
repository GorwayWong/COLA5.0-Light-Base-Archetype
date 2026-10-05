# ToLink DDD 与 COLA 5.0 Light 架构审查

审查日期：2026-10-04。代码基线：`47da1c4`，结论针对本次读取的工作区。未修改业务代码或 AGENTS.md；本次为静态审查，未运行 Maven 构建、ArchUnit 或集成测试。

**结论：当前工程基本符合 COLA 5.0 Light 的分层思想，属于“每个业务上下文内部采用 Light 四层分包”的模块化单体。它已有合适的架构骨架，但尚不能认定为完成了 DDD 实践；架构测试也没有完整执行文档规定的边界。**

## 1. 判定标准

COLA 的官方材料区分架构与组件，不能以是否引入 COLA 组件库判断分层是否成立。[官方说明（v5.0.0）](https://github.com/alibaba/COLA/blob/v5.0.0/README.md)

官方 `cola-archetype-light:5.0.0` 使用单 Maven 项目，在 Java 包内组织 `adapter`、`application`、`domain`、`infrastructure`。它没有要求独立 `client` 模块。官方 service 模板则将 `client`、`app`、`domain`、`infrastructure`、`start` 拆为 Maven 模块。因此，本项目按业务上下文拆 JAR、上下文内部保持四层，是对 Light 的合理扩展，并非原样保留官方单项目模板。[Light 5.0.0 发布模板](https://repo.maven.apache.org/maven2/com/alibaba/cola/cola-archetype-light/5.0.0/cola-archetype-light-5.0.0.jar)、[Light POM](https://github.com/alibaba/COLA/blob/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources/pom.xml)、[Service POM](https://github.com/alibaba/COLA/blob/v5.0.0/cola-archetypes/cola-archetype-service/src/main/resources/archetype-resources/pom.xml)

本次主要检查职责、依赖方向、领域隔离和边界保护。包名相同是结构证据；DDD 还需要从具体业务验证限界上下文、统一语言、聚合、不变量、事务边界和领域行为，不能仅凭目录判定。

## 2. 当前工程与分模块结论

实际 Maven 依赖如下；箭头表示编译依赖：

```text
tolink（父 POM，聚合与版本管理）
├── tolink-shared（普通 JAR，无显式依赖）
├── tolink-agent（普通 JAR） ──→ tolink-shared
└── tolink-bootstrap（可执行 JAR） ──→ tolink-agent、tolink-shared

templates/domain-module（复制模板，不在默认 reactor 中）
```

证据：[根 POM](/Users/gorwaywong/Projects/codeup/tolink/pom.xml:17)、[agent POM](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/pom.xml:18)、[bootstrap POM](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/pom.xml:18)。

| 模块 | 实际内容 | COLA Light / DDD 判定 |
| --- | --- | --- |
| 根 `tolink` | 聚合三个模块、管理 Java 和依赖版本 | 职责合理；父 POM 无须具有四层包 |
| `tolink-agent` | 四层包；只有 infrastructure 有两个实际实现类，其他层仅有 package-info | 分包符合 Light；依赖方向目前没有实际违规证据；DDD 建模待业务实现后验证 |
| `tolink-shared` | 仅 package-info，无显式依赖 | 当前符合纯 Java 公共契约定位；不是业务上下文，无须复制四层 |
| `tolink-bootstrap` | 启动入口、配置属性、安全装配、数据库探针 | 符合装配根定位；不是业务上下文，无须复制四层 |
| `templates/domain-module` | 四层包和 ArchUnit 测试，默认不构建 | 结构符合 Light；复制后的父 POM 路径和边界规则需要修正 |

### tolink-agent

当前包结构是：

```text
org.xaspire.tolink.agent
├── adapter                         package-info
├── application                     package-info
├── domain                          package-info
└── infrastructure
    ├── agentscope                  AgentScopeConfiguration
    └── configuration               AgentProperties
```

`AgentScopeConfiguration` 引用第三方 `ReActAgent`、`Model` 和 Spring，在 infrastructure 进行可选装配，位置正确。[装配代码](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/src/main/java/org/xaspire/tolink/agent/infrastructure/agentscope/AgentScopeConfiguration.java:3)

Spring 与 AgentScope 作为整个 agent JAR 的 Maven 依赖，并不等于 domain 已依赖它们。Light 的层隔离位于包级，必须由代码引用和架构测试判断；单个 JAR 的 POM 无法给四个包分别设置编译 classpath。

目前没有业务实体、值对象、聚合、领域服务、Repository/Gateway 接口、应用用例、DTO 或 Controller。故只能确认骨架适当，不能评价聚合是否过大、是否存在贫血模型、应用层是否承载业务规则、事务是否正确等。[domain 包说明](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/src/main/java/org/xaspire/tolink/agent/domain/package-info.java:1)、[application 包说明](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/src/main/java/org/xaspire/tolink/agent/application/package-info.java:1)

`agent` 被文档定义为业务上下文，但现有实现仅证明 AgentScope 技术能力已接入。它是否具有独立业务语言、规则和生命周期，仍需要具体用例证明；这属于尚未展开的设计，不作为当前缺陷。

### tolink-shared

POM 没有依赖，代码仅声明稳定的跨上下文契约，符合目前定位。[shared POM](/Users/gorwaywong/Projects/codeup/tolink/tolink-shared/pom.xml:14)、[包说明](/Users/gorwaywong/Projects/codeup/tolink/tolink-shared/src/main/java/org/xaspire/tolink/shared/package-info.java:1)

后续只应放多个上下文确实共享、语义稳定的契约。不要把领域实体、某领域的请求对象、持久化对象或厂商 SDK 类型搬入 shared 以绕过边界。技术公共模块本身也不等于 DDD 的 Shared Kernel；后者还涉及共享模型及共同治理。

### tolink-bootstrap

Spring Boot 入口位于 `org.xaspire.tolink`，默认扫描该根包下各模块；全局安全策略和配置位于 bootstrap。唯一可执行模块统一装配领域 JAR，符合模块化单体的装配根职责。[入口](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/main/java/org/xaspire/tolink/Application.java:7)、[插件配置](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/pom.xml:89)

`InfrastructureProbeMapper` 只执行 `SELECT 1`，属于技术探针，放在 bootstrap/persistence 有合理依据；它不是业务 Repository。将数据库、Redis、安全、Flyway 全局装配集中在 bootstrap 是文档明确选择，不构成领域分层违规。[探针](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/main/java/org/xaspire/tolink/bootstrap/persistence/InfrastructureProbeMapper.java:6)、[装配职责](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:50)

未来领域表的 Mapper/PO、业务 Repository 实现、缓存策略、厂商调用应归对应领域的 infrastructure。当前 bootstrap 未见业务用例或领域规则混入。

## 3. 依赖方向与端口归属

项目文档规定的内部依赖方向是合理的：

| 包层 | 允许依赖的项目层 | 主要职责 |
| --- | --- | --- |
| domain | 必要的 shared 契约 | 领域模型、业务不变量、领域服务、领域事件 |
| application | domain、shared | 用例编排、事务协调、应用输入输出契约 |
| adapter | application、shared | HTTP/消息入站、协议与输入转换 |
| infrastructure | application、domain、shared | 实现出站接口，处理持久化和第三方技术 |

表格是项目内层依赖矩阵，不是对 JDK、Spring 应用装配等所有外部依赖的完整白名单。[依赖矩阵](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:28)

需要补充一条明确的接口归属约定：**接口放在需要该能力的内层，技术实现放在 infrastructure。** 官方 Light 示例包含 `domain.gateway`，infrastructure 实现其接口；本项目文档采用 `application.port.out`。后者可以用于应用用例的出站能力，但不能让 domain 为获得外部能力而依赖 application。这是依据官方模板作出的设计判断，不是 COLA 对所有包名的强制规定。[官方 Light 模板源码目录](https://github.com/alibaba/COLA/tree/v5.0.0/cola-archetypes/cola-archetype-light/src/main/resources/archetype-resources/src/main/java)

例如，领域服务需要查询或保存聚合时，可由 domain 定义 Repository/Gateway 接口；应用用例需要通知、调度等能力时，可由 application 定义 `port.out`。不需要为同一能力机械地建立两套接口，也无需提前创建没有用例支撑的空子包。

模板将 repositories/gateways 统称为 infrastructure 内容，未区分接口与实现，后续容易产生归属歧义。建议将文档明确为“infrastructure 保存 Repository/Gateway 的技术实现，接口归调用它的内层”。目前没有相关业务代码，因此这是约定完善项，不是已发生的反向依赖。[模板包契约](/Users/gorwaywong/Projects/codeup/tolink/templates/domain-module/README.md:14)

跨领域使用窄 `application.api` 是可行的同步协作方式。API 的签名也应只暴露稳定契约，不能返回所属领域的实体、PO 或厂商对象。调用方可用自己的出站端口隔离外部领域；领域事件的发送和接收也应通过明确契约与基础设施实现。这些目前都只是文档约定，没有实际跨领域调用可验证。

## 4. 已确认的边界保护与接入问题

以下结论均经主代理复核源码。规则缺口表示“违规可能通过测试”，不表示当前已存在该违规。

| 优先级 | 问题与影响 | 努力 / 修复风险 / 置信度 | 证据 |
| --- | --- | --- | --- |
| P1 | domain 规则未禁止依赖 application，同一上下文的反向依赖可通过；三份规则均遗漏 | S / 低 / 高 | [全局规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:23)、[agent 规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-agent/src/test/java/org/xaspire/tolink/agent/architecture/AgentArchitectureTest.java:14)、[模板规则](/Users/gorwaywong/Projects/codeup/tolink/templates/domain-module/src/test/java/org/xaspire/tolink/template/architecture/DomainArchitectureTest.java:14) |
| P1 | 领域不得依赖 bootstrap 的文档约定未被显式检查；跨领域检查将 bootstrap 排除，不能代替此规则 | S / 低 / 高 | [排除根包](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:126)、[文档约定](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:46) |
| P1 | 模板原位置的父路径是 ../../pom.xml，复制到根目录下一级后应为 ../pom.xml；接入步骤未要求调整，Maven 本地父 POM 定位不正确 | S / 低 / 高 | [模板 POM](/Users/gorwaywong/Projects/codeup/tolink/templates/domain-module/pom.xml:11)、[复制步骤](/Users/gorwaywong/Projects/codeup/tolink/templates/domain-module/README.md:7) |
| P2 | 四层之外的业务包不会被层规则匹配；跨领域目标若在未知层也会被跳过，例如 agent.service，边界保护可被误放包绕开 | M / 低 / 高 | [规则选择器](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:88)、[跳过条件](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:108)、[层识别](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:130) |
| P2 | shared 的规则未禁止依赖 application、adapter、bootstrap，尚未完整保护公共契约模块的独立性；当前无显式依赖 | S / 低 / 高 | [shared 规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:70) |
| P2 | 架构文档仍写 shared/bootstrap/agent，与实际 tolink-* 目录不一致；`-pl bootstrap` 不能按当前目录或 artifactId 选择模块 | S / 低 / 高 | [目录树](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:8)、[构建命令](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:81)、[实际 reactor](/Users/gorwaywong/Projects/codeup/tolink/pom.xml:21) |

模板父路径问题的准确影响是：复制后不能再通过该路径找到本仓库父 POM；Maven 是否继续成功还取决于 reactor/本地仓库的父模型解析，不能将错误相对路径直接等同于所有场景必定构建失败。

全局测试已有 adapter→domain/infrastructure 禁止规则、application→adapter/infrastructure 禁止规则、infrastructure→adapter 禁止规则、根包切片无环规则，以及跨上下文只能访问 `application.api` 的检查。这些是有效的保护，不应因存在缺口而全部推翻。[现有层规则](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:38)、[跨领域检查](/Users/gorwaywong/Projects/codeup/tolink/tolink-bootstrap/src/test/java/org/xaspire/tolink/architecture/ArchitectureTest.java:111)

领域纯净性的框架禁用规则采用列举方式，覆盖 Spring、MyBatis-Plus、AgentScope 等已选技术，并非完整“纯 Java”证明。应用层也没有厂商 SDK 类型的专门约束。当前只有 infrastructure 引用这些技术，未发现实际泄漏；后续增加技术栈时应同步维护约束。

## 5. DDD 完成度与下一步

当前能确认的是业务上下文按 JAR 隔离、四层职责声明、shared/装配根分离，以及部分可执行的包边界规则。无法确认的是业务上下文划分是否贴合业务、统一语言是否一致、聚合和不变量是否合理、应用事务边界是否正确、跨上下文数据所有权与协作是否成立。文档列出的 identity、far、membership、persona、recommendation、workflow、connection 尚未加入 reactor，不能给这些未来模块出具合规结论。[当前与未来领域说明](/Users/gorwaywong/Projects/codeup/tolink/docs/architecture.md:26)

建议先补齐已有约束和模板接入步骤，再用 agent 的一个真实业务用例检验模型与调用链。优先澄清 agent 的业务职责和端口归属，不需要先把每个领域拆成四个 Maven 子模块，也不需要先新增 client、common、platform 模块。

后续修复完成后，适当的基本验证命令为：

```bash
rtk proxy ./mvnw -pl tolink-bootstrap -am verify
```

架构测试应增加能证明规则有效的违规样例，重点覆盖 domain→application、领域→bootstrap、shared→领域应用、未知层的跨领域访问；模板应在仓库内临时复制并调整父路径后验证接入。此处只提出验证方向，尚未执行或编写这些测试。

## 6. 审查范围与限制

覆盖全部现有模块的 POM、生产 Java 分包与引用、三份架构测试、模板说明、架构文档和技术基线；核对了官方 v5.0.0 Light/Service 模板。当前三个实际模块共 13 个生产 Java 文件，其中 7 个实际类/接口、6 个 package-info；业务骨架很小，结论没有业务实现上的统计意义。

未审查业务功能正确性、完整安全性、性能、数据库建模或依赖版本支持状态；未启动应用、未运行 Docker 或测试。技术基线中记录的历史验证结果不作为本次重新通过的证据。项目没有现成 graphify 图，本次采用源码与依赖的只读审查，未生成知识图谱。
