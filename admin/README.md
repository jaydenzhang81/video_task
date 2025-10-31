# 视频任务管理系统 - 管理后台

## 项目简介

这是一个基于 Vue 3 + Element Plus 的视频任务管理系统管理后台，主要用于管理视频内容、达人用户、任务分配和财务数据。

## 功能特性

### 🏠 工作台
- 数据概览仪表板
- 实时统计图表
- 达人排行榜
- 平台数据筛选

### 📹 视频管理
- 视频列表管理
- 视频上传和编辑
- 视频分类管理
- 任务设置

### 📋 任务管理
- 任务创建和编辑
- 任务状态管理
- 参与用户统计
- 任务详情查看

### 💰 财务管理
- 财务数据概览
- 提现记录管理
- 批量提现操作
- 财务统计报表

### 👥 达人管理
- 达人用户管理
- 团队管理
- 达人数据统计
- 达人详情查看

### 🏆 榜单管理
- 达人排行榜
- 多维度榜单筛选
- 榜单数据导出
- 实时榜单更新

### ⚙️ 系统管理
- 管理员用户管理
- 系统设置配置
- 权限管理
- 系统监控

## 技术栈

- **前端框架**: Vue 3
- **UI 组件库**: Element Plus
- **图表库**: ECharts
- **路由管理**: Vue Router 4
- **构建工具**: Vite
- **包管理器**: npm

## 项目结构

```
admin/
├── src/
│   ├── components/          # 公共组件
│   ├── layout/             # 布局组件
│   │   └── MainLayout.vue  # 主布局
│   ├── router/             # 路由配置
│   │   └── index.js
│   ├── views/              # 页面组件
│   │   ├── Dashboard.vue   # 工作台
│   │   ├── VideoManagement.vue # 视频管理
│   │   ├── TaskManagement.vue  # 任务管理
│   │   ├── FinanceManagement.vue # 财务管理
│   │   ├── InfluencerManagement.vue # 达人管理
│   │   ├── RankingManagement.vue # 榜单管理
│   │   ├── SystemManagement.vue # 系统管理
│   │   └── Login.vue       # 登录页面
│   ├── utils/              # 工具函数
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── public/                 # 静态资源
├── index.html              # HTML 模板
├── package.json            # 项目配置
├── vite.config.js          # Vite 配置
└── README.md               # 项目说明
```

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 登录信息

- **用户名**: admin
- **密码**: 123456

## 数据库表结构

### 主要数据表

1. **tb_admin** - 管理员用户表
2. **tb_video** - 视频主表
3. **tb_video_task** - 视频任务分享奖励金额表
4. **tb_user** - 普通用户表
5. **tb_influencer_user** - 达人用户关联表
6. **tb_influencer_team** - 达人团队表
7. **tb_video_share_user_log** - 视频分享用户日志表

## 功能截图

### 工作台
- 数据概览卡片
- 实时统计图表
- 达人排行榜

### 视频管理
- 视频列表
- 视频上传
- 任务设置

### 任务管理
- 任务列表
- 任务详情
- 参与统计

### 财务管理
- 财务概览
- 提现记录
- 批量操作

### 达人管理
- 达人列表
- 团队管理
- 数据统计

### 榜单管理
- 排行榜
- 多维度筛选
- 数据导出

### 系统管理
- 管理员管理
- 系统设置
- 权限控制

## 开发说明

### 组件开发规范

1. 使用 Vue 3 Composition API
2. 组件命名采用 PascalCase
3. 文件命名采用 PascalCase.vue
4. 使用 TypeScript 类型注解（可选）

### 样式规范

1. 使用 scoped 样式
2. 采用 BEM 命名规范
3. 使用 CSS 变量管理主题色
4. 响应式设计

### 代码规范

1. 使用 ESLint 进行代码检查
2. 使用 Prettier 进行代码格式化
3. 遵循 Vue 3 官方风格指南
4. 添加必要的注释

## 部署说明

### 开发环境

```bash
npm run dev
```

### 生产环境

```bash
npm run build
```

构建完成后，将 `dist` 目录下的文件部署到 Web 服务器。

## 更新日志

### v1.0.0 (2024-01-15)
- 初始版本发布
- 完成基础功能开发
- 实现完整的 CRUD 操作
- 添加数据可视化图表

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请通过以下方式联系：

- 邮箱: admin@example.com
- 项目地址: https://github.com/example/video-task-admin
