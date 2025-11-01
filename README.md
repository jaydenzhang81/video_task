## 视频任务管理系统（Admin + Mobile + Backend）

一个包含管理后台、移动端与后端服务的完整视频任务管理系统。项目分为三个模块：

- `admin`：基于 Vue 3 + Vite 的管理后台
- `mobile`：基于 UniApp 的多端移动应用（H5/微信小程序/App）
- `backend`：基于 Spring Boot 的后端服务

### 目录结构

```
video_task/
├─ admin/                # 管理后台（Vue 3 + Vite）
├─ mobile/               # 移动端（UniApp）
└─ backend/              # 后端（Spring Boot）
```

### 技术栈

- **前端（Admin）**: Vue 3, Vue Router 4, Pinia, Element Plus, Vite, Axios
- **移动端（Mobile）**: UniApp（H5/小程序/App 多端）、自定义请求封装
- **后端（Backend）**: Spring Boot 2.7.x, Spring Data JPA, MySQL, Redis, Sa-Token, Hutool

### 环境要求（Windows 11 / PowerShell）

- Node.js ≥ 16（建议 LTS）
- npm ≥ 8
- JDK 8（与 `backend/pom.xml` 中 `java.version=8` 一致）
- Maven ≥ 3.6
- MySQL、Redis 已可用（数据库/表已就绪；本项目不提供建表 SQL）
- 可选：HBuilderX（便于 UniApp 调试与打包）

---

## 快速开始（本地开发）

> 以下命令均在 PowerShell 中执行。

### 1) 启动后端（Backend，端口默认 `8080`）

1. 调整 `backend/src/main/resources/application.yml` 中的数据库、Redis 与第三方配置（如有需要）。
2. 编译与运行：

```powershell
cd backend
mvn -v
mvn clean package -DskipTests
java -jar target\video-task-backend-1.0.0.jar
```

启动成功后，服务监听 `http://localhost:8080`。

### 2) 启动管理后台（Admin，开发端口 `3000`）

Admin 已通过 Vite 代理将 `/api` 转发至 `http://localhost:8080`（见 `admin/vite.config.js`）。开发环境无需额外修改。

```powershell
cd admin
npm install
npm run dev
```

浏览器访问 `http://localhost:3000`。

### 3) 启动移动端（Mobile）

移动端请求基地址默认为空，可在 `mobile/utils/request.js` 设置：

```js
// mobile/utils/request.js
// 将 config.baseURL 配置为后端地址，例如：
// config.baseURL = 'http://localhost:8080'
```

安装依赖并启动对应平台：

```powershell
cd mobile
npm install

# H5 调试（编译到 dev 目录，按需结合本地静态服务或 HBuilderX 预览）
npm run dev:h5

# 微信小程序（生成到 mp-weixin 目录，配合微信开发者工具）
npm run dev:mp-weixin

# App（生成到 app-plus 目录，配合 HBuilderX 或真机）
npm run dev:app-plus
```

---

## 构建与部署

### 后端（Backend）

```powershell
cd backend
mvn clean package -DskipTests
java -jar target\video-task-backend-1.0.0.jar
```

- 生产环境按需配置 `application.yml`（数据库、Redis、token、第三方平台密钥等）。
- Windows 可使用 NSSM/服务管理工具将 Jar 注册为系统服务。

### 管理后台（Admin）

开发环境使用代理 `/api -> http://localhost:8080`。生产部署建议：

1. 构建静态资源：

```powershell
cd admin
npm run build
```

2. 将 `admin/dist` 部署到任意静态服务器（Nginx/IIS 等）。
3. 使用反向代理将前端 `/api` 转发到后端地址（与开发环境保持一致）。

如需在生产中改用绝对地址，可在 `admin/src/utils/api.js` 调整 `baseURL`，或在网关层保持 `/api` 前缀一致性，避免改动前端代码。

### 移动端（Mobile）

1. 设置后端地址：在 `mobile/utils/request.js` 统一配置 `config.baseURL`。
2. 按目标平台构建：

```powershell
cd mobile

# H5 构建
npm run build:h5

# 微信小程序构建（使用微信开发者工具导入产物）
npm run build:mp-weixin

# App 构建（建议配合 HBuilderX 打包）
npm run build:app-plus
```

> 说明：`package.json` 中的 `dev:*`/`build:*` 均调用 `uni build`，可结合 HBuilderX/各平台工具进行预览与打包。

---

## 接口地址与跨域说明

- 后端默认端口：`8080`
- Admin（开发）：Vite 代理 `/api -> http://localhost:8080`，无需额外配置。
- Admin（生产）：建议在网关/Nginx 保持 `/api` 前缀并反向代理到后端；或在 `admin/src/utils/api.js` 显式设置 `baseURL` 指向后端。
- Mobile：在 `mobile/utils/request.js` 配置 `config.baseURL`，例如 `http://your-server:8080`。

如遇 H5 跨域：

- 开发阶段使用 Vite 代理（已内置）。
- 生产阶段使用 Nginx 反向代理，保持 `/api` 前缀一致；或在后端开启 CORS（若已实现）。

---

## 常见问题（FAQ）

- **端口冲突**：如 `8080`/`3000` 被占用，请在后端 `application.yml` 或 Vite `server.port` 中调整。
- **前端部署后接口 404**：确认生产环境的 `/api` 反向代理已指向后端，或在前端显式配置绝对 `baseURL`。
- **单页应用刷新 404**：Admin 为 SPA，Nginx 需增加 `try_files`（将未知路由回退到 `index.html`）。
- **移动端无法请求**：确保 `config.baseURL` 指向可达的后端，且小程序/App 平台已配置域名白名单/网络权限。

---

## 版本信息

- Admin：`video-task-admin@1.0.0`（Vite，开发端口 3000）
- Mobile：`video-task-mobile@1.0.0`（UniApp 多端）
- Backend：`video-task-backend@1.0.0`（默认端口 8080）

---

## 相关文档（模块内）

- 管理后台说明：`admin/README.md`
- 移动端说明：`mobile/README.md`


