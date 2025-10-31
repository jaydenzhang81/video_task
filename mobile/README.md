# 视频任务管理系统 - 移动端

基于 UniApp 开发的跨平台移动应用，支持 H5、微信小程序、App 等多端运行。

## 技术栈

- **框架**: UniApp (Vue 2)
- **UI组件**: 原生组件 + 自定义组件
- **状态管理**: Vue 2 响应式数据
- **网络请求**: uni.request + 自定义封装
- **本地存储**: uni.storage + 自定义封装

## 项目结构

```
mobile/
├── components/          # 公共组件
│   ├── VideoCard.vue    # 视频卡片组件
│   └── PlatformSelector.vue # 平台选择器组件
├── pages/              # 页面文件
│   ├── index/          # 首页
│   ├── login/          # 登录页
│   ├── video-data/     # 视频数据页
│   ├── ranking/        # 排行榜页
│   └── profile/        # 个人中心页
├── static/             # 静态资源
│   └── images/         # 图片资源
├── utils/              # 工具类
│   ├── api.js          # API接口封装
│   ├── request.js      # 请求拦截器
│   └── storage.js      # 本地存储工具
├── App.vue             # 应用入口组件
├── main.js             # 应用入口文件
├── manifest.json       # 应用配置文件
├── pages.json          # 页面路由配置
├── uni.scss            # 全局样式变量
├── package.json        # 项目依赖配置
└── vue.config.js       # Vue配置文件
```

## 功能模块

### 1. 用户认证
- 手机号密码登录
- JWT Token 认证
- 自动登录状态检查
- 登录状态持久化

### 2. 首页功能
- 视频列表展示
- 视频分类筛选
- 平台选择器
- 视频发布功能
- 播放数据展示

### 3. 视频数据
- 数据统计展示
- 时间范围筛选
- 平台数据对比
- 发布记录查看

### 4. 个人中心
- 用户信息展示
- 设置管理
- 退出登录

## 开发环境

### 环境要求
- Node.js >= 12.0.0
- HBuilderX (推荐) 或 Vue CLI
- 微信开发者工具 (小程序开发)

### 安装依赖
```bash
npm install
```

### 开发运行

#### H5 开发
```bash
npm run dev:h5
```

#### 微信小程序开发
```bash
npm run dev:mp-weixin
```

#### App 开发
```bash
npm run dev:app-plus
```

### 构建发布

#### H5 构建
```bash
npm run build:h5
```

#### 微信小程序构建
```bash
npm run build:mp-weixin
```

#### App 构建
```bash
npm run build:app-plus
```

## 配置说明

### manifest.json
- 应用基本信息配置
- 平台特定配置
- 权限配置

### pages.json
- 页面路由配置
- 导航栏样式
- TabBar 配置

### vue.config.js
- 开发服务器配置
- 代理配置
- 构建配置

## API 接口

### 基础配置
- 基础URL: `http://localhost:8080/api`
- 请求超时: 10秒
- 认证方式: Bearer Token

### 主要接口
- `POST /user/login` - 用户登录
- `GET /platform/list` - 获取平台列表
- `GET /video/types` - 获取视频类型
- `GET /video/list` - 获取视频列表
- `POST /video/share` - 分享视频

## 组件说明

### VideoCard 组件
视频卡片组件，用于展示视频信息和操作按钮。

**Props:**
- `video`: 视频对象

**Events:**
- `click`: 点击视频卡片
- `publish`: 点击发布按钮

### PlatformSelector 组件
平台选择器组件，用于选择发布平台。

**Props:**
- `platforms`: 平台列表
- `visible`: 是否显示

**Events:**
- `cancel`: 取消选择
- `confirm`: 确认选择

## 工具类

### Storage 工具类
本地存储管理工具，提供统一的存储接口。

**主要方法:**
- `set(key, value)` - 设置存储
- `get(key, defaultValue)` - 获取存储
- `remove(key)` - 删除存储
- `clear()` - 清空所有存储

### Request 工具类
网络请求工具，提供统一的请求接口和错误处理。

**主要方法:**
- `get(url, data, options)` - GET请求
- `post(url, data, options)` - POST请求
- `upload(url, filePath, name, formData, options)` - 文件上传

## 注意事项

1. **跨域问题**: 开发时需要在后端配置 CORS 或使用代理
2. **Token 管理**: 自动处理 Token 过期和刷新
3. **错误处理**: 统一的错误处理和用户提示
4. **平台差异**: 注意不同平台的 API 差异
5. **性能优化**: 合理使用缓存和懒加载

## 常见问题

### Q: 如何修改后端接口地址？
A: 修改 `utils/api.js` 中的 `BASE_URL` 配置。

### Q: 导入路径报错怎么办？
A: 在UniApp项目中，建议使用相对路径导入，如 `import api from '../../utils/api.js'`。如果使用HBuilderX，可以在项目设置中配置路径别名。

### Q: 如何添加新的页面？
A: 在 `pages` 目录下创建页面文件，并在 `pages.json` 中配置路由。

### Q: 如何处理登录状态？
A: 使用 `UserStorage` 工具类管理登录状态，应用启动时自动检查。

### Q: 如何自定义主题？
A: 修改 `uni.scss` 中的样式变量，或修改 `App.vue` 中的全局样式。
