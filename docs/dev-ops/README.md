# 环境与应用运行契约

本目录只提供环境定义、Compose 和配置；Maven 构建、Docker build、Registry 和 CI/CD 由使用方安排。应用 Compose 只运行 APP_IMAGE，没有 build。

## 文件

- docker-compose-environment.yml：PostgreSQL 17、Redis 7、持久卷、健康检查和网络，不发布端口。
- docker-compose-environment-dev.yml：开发 override，仅向 127.0.0.1 发布数据库/Redis端口。
- docker-compose-app.yml：运行已有 APP_IMAGE，默认 prod，加入环境的外部网络。
- dev.env.example：可运行的本地示例；prod.env.example：生产必填配置清单。

从仓库根目录执行，复制示例为 dev.env/prod.env，不提交真实配置。Compose 读取 env 文件不会替本地 JVM 设置环境变量。

## 开发

```bash
cp docs/dev-ops/env/dev.env.example docs/dev-ops/env/dev.env
docker compose --env-file docs/dev-ops/env/dev.env \
  -f docs/dev-ops/compose/docker-compose-environment.yml \
  -f docs/dev-ops/compose/docker-compose-environment-dev.yml up -d --wait
```

开发 JVM 使用 localhost，应用容器使用 postgres、redis。修改 POSTGRES_PORT/REDIS_PORT 时同步本地 SPRING_DATASOURCE_URL / SPRING_DATA_REDIS_PORT；修改账号密码时同步本地 SPRING_*。

停止环境使用相同 env 和两个 -f 参数，将 up 替换为 down；命名卷保留数据。清空持久卷必须由使用方另行决定。

## 生产运行

填写 prod.env，至少设置 APP_IMAGE、POSTGRES_PASSWORD、REDIS_PASSWORD。使用已发布的不可变版本标签或 digest：

```bash
docker compose --env-file docs/dev-ops/env/prod.env \
  -f docs/dev-ops/compose/docker-compose-environment.yml up -d --wait
docker compose --env-file docs/dev-ops/env/prod.env \
  -f docs/dev-ops/compose/docker-compose-app.yml pull
docker compose --env-file docs/dev-ops/env/prod.env \
  -f docs/dev-ops/compose/docker-compose-app.yml up -d --wait
```

两个 Compose 的 COMPOSE_PROJECT_NAME 必须一致。不要使用 --remove-orphans，以免单独操作一个 Compose 时误删另一个文件管理的服务。

prod 关闭文档并默认拒绝业务请求；业务上线先配置认证授权。应用默认绑定宿主机 127.0.0.1，需要直接发布时显式设置 APP_BIND_ADDRESS，或接入反向代理。

## 变量契约

| 变量 | 读取方 | 用途 |
| --- | --- | --- |
| COMPOSE_PROJECT_NAME | Compose | 环境与 app 共用网络/卷命名 |
| POSTGRES_DB/USER/PASSWORD | environment/app Compose | 初始化数据库；生成容器 JDBC 配置 |
| REDIS_PASSWORD | environment/app Compose | Redis 与应用认证 |
| POSTGRES_PORT/REDIS_PORT | dev override | 宿主机端口 |
| SPRING_PROFILES_ACTIVE | JVM/app Compose | 显式 dev/prod；app 默认 prod |
| SPRING_DATASOURCE_URL/USERNAME/PASSWORD | JVM | prod 必填；app Compose 从 POSTGRES_* 生成 |
| SPRING_DATA_REDIS_HOST/PORT/PASSWORD | JVM | prod host/password 必填；app Compose 自动生成 |
| APP_IMAGE | app Compose | 已构建镜像，必填 |
| APP_PORT/APP_BIND_ADDRESS | app Compose | 宿主机监听；容器内8080 |
| DB_POOL_MAX_SIZE | JVM/app Compose | Hikari 最大连接数，默认10 |
| JAVA_TOOL_OPTIONS | JVM/app Compose | JVM 参数；含空格时 dotenv 值需引号 |

prod.env.example 的密码和 APP_IMAGE 故意留空，未填写时 Compose 校验失败；生产 YAML 无开发密码兜底。公共 application.yml 不选择环境；dev 提供示例值，prod 必填注入，integration 仅用于测试。备份、TLS、资源限制和密钥管理由部署方决定。

env 示例中的 SPRING_DATASOURCE_* / SPRING_DATA_REDIS_* 用于直接运行 JVM；使用 app Compose 时，这些连接值由 POSTGRES_* / REDIS_PASSWORD 和服务名生成。需要连接外部托管数据库或 Redis 时，应调整 app Compose 的连接映射，不能仅修改 env 文件的 SPRING_* 值。
