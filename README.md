# COLA 5.0 Light Modular Monolith

一个业务领域对应一个 Maven Module，内部按 api / adapter / application / domain / infrastructure 分层。bootstrap 是唯一组合根，最终运行一个 Spring Boot JAR。project-sample 是可复制的空白领域模板。

```text
.
├── pom.xml
├── AGENTS.md
├── project-bootstrap
├── project-shared
├── project-sample
│   ├── README.md
│   ├── src/main/java
│   └── src/test/java
└── docs/dev-ops
    ├── README.md
    ├── compose
    │   ├── docker-compose-environment.yml
    │   ├── docker-compose-environment-dev.yml
    │   └── docker-compose-app.yml
    └── env
        ├── dev.env.example
        └── prod.env.example
```

## 构建

需要 JDK21+，编译与容器目标为 Java21。使用 Maven Wrapper 或 Maven3.9+：

```bash
./mvnw clean verify
# Windows
./mvnw.cmd clean verify
# 只检查领域模板及其依赖
./mvnw -pl project-sample -am clean verify
```

默认执行模块与全局架构规则、合法/违规依赖样例，无需数据库。模板生产源码只有 package-info.java，空 JAR 提示符合模板状态；只有 bootstrap 生成可执行 Boot JAR。复制入口为 [project-sample](project-sample/README.md)。

## 开发运行

从仓库根目录执行：

```bash
cp docs/dev-ops/env/dev.env.example docs/dev-ops/env/dev.env
docker compose --env-file docs/dev-ops/env/dev.env \
  -f docs/dev-ops/compose/docker-compose-environment.yml \
  -f docs/dev-ops/compose/docker-compose-environment-dev.yml up -d --wait
./mvnw -pl project-bootstrap -am package -DskipTests
java -jar project-bootstrap/target/project-bootstrap-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

默认开发配置对应 env 示例。修改端口或凭据时，将同样的 SPRING_* 值传给本地 JVM；Compose 不会替宿主机 Java 设置环境变量。Bash/Git Bash 可使用 `set -a; source docs/dev-ops/env/dev.env; set +a`；PowerShell 使用 `$env:SPRING_DATASOURCE_URL` 等变量。

健康探针为 /actuator/health、/actuator/health/liveness、/actuator/health/readiness；dev 文档入口为 /swagger-ui/index.html。模板没有业务端点。prod 关闭文档，所有环境都默认拒绝尚未配置授权的业务请求。显式选择 dev/prod profile。

## 基础设施测试

开发环境启动后执行 `./mvnw -Pintegration verify`，使用 SPRING_* 连接 PostgreSQL17、Redis7。bootstrap 测试夹具验证健康端点、Flyway、MyBatis-Plus、数据库回滚、JSON和Redis TTL/锁。

测试 SQL 位于测试资源 db/integration-migration，使用独立 integration_probe.integration_flyway_schema_history；MyBatis探针表为事务内的 PostgreSQL临时表。测试夹具不进入生产 JAR，也不占用 public 业务迁移历史；领域模板不依赖 bootstrap。不要连接生产数据库。

## 扩展与部署

- [领域模板与复制步骤](project-sample/README.md)
- [架构契约](docs/architecture.md)
- [环境与部署](docs/dev-ops/README.md)
- [技术基线](docs/technical-baseline.md)

构建镜像由使用方安排：先 Maven package，再 `docker build -t project-bootstrap:local project-bootstrap`。Dockerfile 位于 bootstrap；docs/dev-ops 仅管理环境与 APP_IMAGE 运行契约。

模板没有业务迁移，Flyway 为实际领域的 SQL 预留入口。如果已在旧示例数据库应用 V1__sample_create_samples.sql，请为新骨架选择新数据库；有数据的旧库需先制定兼容迁移方案，不执行 repair、清库或删卷来绕过历史校验。
