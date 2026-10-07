# Sample 领域模块模板改造与验证

## 交付结果

依据 COLA_5_Light_Project_Sample_Module_Design.md，project-sample 已成为可复制的空白领域模块模板。

- 生产源码仅有31个 package-info.java，覆盖五层和规定的子包。
- 不包含 Sample Entity、Controller、Repository、Facade、业务用例或迁移SQL。
- 模块 README 说明包职责、依赖方向、跨模块通信、组合根与完整复制步骤。
- 模块内 ArchUnit测试随复制带走，允许空层，同时用真实违规依赖夹具证明规则有效。
- project-sample 是唯一复制入口；重复的 templates/domain-module 已移除。
- bootstrap 的 SampleComposition、Sample接口放行开关与专属装配已移除；运行能力保持为公共基础设施和健康端点。

模块说明：[project-sample/README.md](../project-sample/README.md)。
当前全局契约：[architecture.md](architecture.md)。

## 包与测试职责

```text
api/{facade,dto,command,query}
adapter/{web,mq,job}
application/{service,command,query,assembler}
domain/model/{aggregate,entity,valobj}
domain/{service,repository,port,event}
infrastructure/persistence/{mapper,dataobject,repository}
infrastructure/{gateway,client}
```

每个领域只有一个 Maven模块，技术依赖对全部内部包可见；架构规则保护包级边界。domain只依赖自身domain和JDK核心值类型；api不暴露内部模型或技术类型。跨模块调用只访问对方api；公开事件契约放api，由application映射内部领域事件。

ModuleArchitectureTest检查领域纯净、五层方向、API技术类型和跨模块API边界。ModuleArchitectureRulesTest验证合法依赖通过，层反转、框架泄漏、API泄漏、跨模块内部引用被拒绝。夹具仅位于test；integration包只预留测试职责，没有虚构业务测试。

## 已确认的 Spring Service 政策

用户选择：application允许 @Service，但实现仍由bootstrap显式注册，启动入口只扫描bootstrap。

全局规则已相应调整并验证：
- application的@Service可以通过；
- application的@Configuration被拒绝，即使同时标注@Service；
- infrastructure/shared的自动组件配置仍被拒绝；
- domain保持零框架依赖。

新增业务能力时，bootstrap使用@Bean或明确@Import注册实现，用@MapperScan注册Mapper。模板没有需要装配的业务实现。

## 基础设施测试隔离

bootstrap保留真实 PostgreSQL/MyBatis-Plus、事务回滚、Flyway、Redis TTL/租约锁和JSON检查：
- Mapper/DO只在测试源码中。
- PostgreSQL探针使用事务内临时表，事务结束自动移除。
- Flyway只在测试中读取db/integration-migration，写integration_probe.integration_flyway_schema_history。
- 技术测试不占用public业务迁移历史，也不创建Sample schema。
- 测试Mapper/DO/SQL不进入可执行JAR。

生产Flyway继续扫描实际领域的classpath:db/migration。空模板没有生产迁移，在新数据库上可以正常启动；业务迁移由复制后的实际领域按需要提供，版本号在整个仓库唯一。

## 验证结果

| 检查 | 结果 |
| --- | --- |
| 根Maven Wrapper clean verify | 通过，默认29项 |
| 模块本地架构检查 | 4条生产规则 + 10个合法/违规验证 |
| bootstrap全局架构检查 | 6条生产规则 + 9个合法/违规验证 |
| 最终-Pintegration verify | 通过，29项默认测试 + 5项真实集成测试，共34项，零失败/零错误 |
| 模板复制后构建 | 重命名为独立模块/包，在临时reactor中clean verify，14项测试通过 |
| Sample生产源码检查 | 31份包文档，0个业务Java类 |
| Sample JAR检查 | 0个业务class |
| bootstrap JAR检查 | 测试Mapper、DO、测试迁移与SampleComposition泄漏数量为0 |
| Java21镜像构建 | 成功 |
| 先执行集成测试，再启动dev容器 | 成功，overall/liveness/readiness全部UP |
| dev OpenAPI | paths为空，无业务端点 |
| prod容器 | 健康端点200，OpenAPI/Swagger均403 |
| 数据库隔离检查 | public测试历史表0、独立schema测试历史表1、Sample schema0 |
| git diff --check | 通过 |

验证环境沿用 Java21目标、Maven Wrapper3.9.16、PostgreSQL17、Redis7；宿主机JDK25执行测试，镜像使用Java21。没有改动技术组件版本。

临时复制件、验证配置、容器、网络和本轮测试卷已清理。验证镜像 cola-sample-template-verification:local 保留本地，未发布。

## 使用与兼容性

复制主/测试源码、POM与README，排除target；替换artifactId与main/test两处模块包名，注册根POM与bootstrap依赖后即可开始开发。模块README给出完整步骤。

如果已有数据库应用上一轮V1__sample_create_samples.sql，建议为这份空白骨架选择新数据库；有数据的旧库需要先制定兼容迁移方案。此次源码改造没有对用户数据库执行repair、清库或删卷操作，数据库清理仅限本轮独立验证项目。

根AGENTS.md按其规则保持原样。第一轮implementation-report.md已标为历史报告；当前规范以模块README、architecture.md与dev-ops文档为准。
