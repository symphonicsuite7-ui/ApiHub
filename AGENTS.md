# AGENTS.md

这个仓库是一个基于 Java 的多模块 API 平台，前端为 Vue 3 管理后台。修改时请保持范围限定在相关模块，并遵循项目现有的 Spring Boot 约定。

## 项目地图

- 后端根目录：由 [pom.xml](pom.xml) 统一管理的 Maven 多模块构建
- 模块职责：
  - [api-common](api-common)：共享模型、常量、响应包装器、工具代码
  - [api-gateway](api-gateway)：统一入口、路由、鉴权检查、链路追踪处理
  - [api-auth](api-auth)：认证、登录、JWT 签发、用户管理
  - [api-admin](api-admin)：API/资源管理、应用管理、审计数据
  - [api-invoke](api-invoke)：开放 API 调用的示例业务服务
- 前端：[web-admin](web-admin)，使用 Vue 3 + Vite + TypeScript
- 部署文档：[docs/需求与开发方向.md](docs/需求与开发方向.md)
- 基础设施：[docker-compose.yml](docker-compose.yml)、[nginx/nginx.conf](nginx/nginx.conf)

## 技术基线

- Java 17
- Spring Boot 3.3.5
- Maven 多模块构建
- 运行时依赖 MySQL + Redis
- 前端使用 Vite 和 Element Plus

## 核心命令

在仓库根目录运行：

```bash
./mvnw -q test
./mvnw -q -pl api-auth -am test
./mvnw -q -pl api-gateway -am test
./mvnw -q -pl api-admin -am test
./mvnw -q -pl api-invoke -am test
```

构建前端：

```bash
cd web-admin
npm install
npm run build
```

本地基础设施：

```bash
docker compose up -d mysql redis
```

## 工作规范

- 优先做模块内修改，避免跨模块、跨层的大面积改动。
- 共享逻辑放在 [api-common](api-common)；避免在各服务间重复定义响应 DTO 或常量。
- API 网关职责要与业务逻辑分离；认证、路由、链路追踪属于网关或共享安全工具类，而不是业务模块。
- 遵循各模块 `src/main/java` 目录下的 Spring Boot 命名与包结构约定。
- 对任何带有行为变更的修改都应同步补充或更新测试；优先使用模块级测试，而不是假设全仓库范围没有副作用。
- 对于跨后端与前端的功能，需保持 API 契约与后端模块职责一致。

## 重要说明

- 该项目设计为多服务架构，而不是单体应用；不要把无关业务逻辑合并到一个模块中。
- 产品方案和需求文档位于 [docs/需求与开发方向.md](docs/需求与开发方向.md)，应将其作为功能意图的主要依据。
- 前端改动位于 [web-admin/src](web-admin/src)；后端改动位于各个独立的 `api-*` 模块中。

## Agent 推荐工作流

1. 在修改前，根据功能区域先定位受影响模块。
2. 在引入新抽象前，先检查最接近的现有服务包和代码模式。
3. 使用该模块最小范围相关的 Maven 命令进行验证。
4. 保持文档和配置与当前服务架构、部署文件保持一致。

> 以 [docs/需求与开发方向.md](docs/需求与开发方向.md) 作为产品范围和里程碑的权威参考。
