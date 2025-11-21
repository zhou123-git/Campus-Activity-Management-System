# 项目架构文档

## 1. 项目概述

本项目是一个活动管理系统，采用前后端分离架构设计。后端使用Java原生HttpServer提供RESTful API接口，前端使用Vue和React框架结合Ant Design Element UI组件库以及TailwindCSS样式框架构建现代化用户界面。

## 2. 技术栈

### 后端技术栈
- **编程语言**: Java (JDK 22)
- **数据库**: MySQL
- **数据库连接**: JDBC
- **Web服务器**: 原生HttpServer (com.sun.net.httpserver)
- **项目构建**: 原生Java编译

### 前端技术栈
- **核心框架**: Vue.js + React
- **UI组件库**: Ant Design Element
- **样式框架**: TailwindCSS
- **HTTP客户端**: Fetch API
- **构建工具**: Vite/Webpack

## 3. 项目目录结构

```
D:\code\test_000\
├── backend/                   # 后端代码目录
│   ├── src/
│   │   ├── controller/        # 控制器层，处理HTTP请求
│   │   ├── service/           # 业务逻辑层
│   │   ├── entity/            # 实体类
│   │   ├── dao/               # 数据访问对象层
│   │   ├── db/                # 数据库连接配置
│   │   ├── util/              # 工具类
│   │   ├── Server.java        # HTTP服务器入口
│   │   └── App.java           # 控制台应用入口（可选）
│   ├── lib/                   # 外部依赖库
│   └── out/                   # 编译输出目录
├── frontend/                  # 前端代码目录
│   ├── public/                # 静态资源目录
│   ├── src/
│   │   ├── assets/            # 静态资源（图片、字体等）
│   │   ├── components/        # Vue/React通用组件
│   │   ├── views/             # 页面视图组件
│   │   ├── router/            # 路由配置
│   │   ├── store/             # 状态管理
│   │   ├── utils/             # 工具函数
│   │   ├── services/          # API服务封装
│   │   ├── App.vue            # Vue根组件
│   │   ├── App.jsx            # React根组件
│   │   ├── main.js            # Vue入口文件
│   │   └── index.js           # React入口文件
│   ├── package.json           # npm包配置文件
│   ├── vite.config.js         # Vite构建配置
│   └── tailwind.config.js     # TailwindCSS配置文件
├── docs/                      # 文档目录
├── scripts/                   # 脚本目录（构建、部署等）
├── .gitignore                 # Git忽略文件
├── README.md                  # 项目说明文档
├── sources.txt                # 数据源说明
└── test_000.iml               # IntelliJ IDEA项目配置文件
```

## 4. 后端模块设计

### 分层架构
采用经典的MVC分层架构模式：

1. **Controller层**（控制层）
    - UserController.java - 处理用户相关接口
    - ActivityController.java - 处理活动相关接口
    - RegistrationController.java - 处理报名相关接口

2. **Service层**（业务逻辑层）
    - UserService.java - 用户业务逻辑
    - ActivityService.java - 活动业务逻辑
    - RegistrationService.java - 报名业务逻辑

3. **DAO层**（数据访问层）
    - UserDao.java - 用户数据操作
    - ActivityDao.java - 活动数据操作
    - RegistrationDao.java - 报名数据操作

4. **Entity层**（实体类）
    - User.java - 用户实体
    - Activity.java - 活动实体
    - Registration.java - 报名实体

5. **DB层**（数据库连接）
    - DB.java - 数据库连接配置

6. **Util层**（工具类）
    - JsonUtil.java - JSON处理工具
    - ValidationUtil.java - 数据验证工具

### 统一接口规范

#### 接口约定
- 所有接口均以 `/api` 开头
- 使用 RESTful 风格设计
- 请求方法：POST (application/x-www-form-urlencoded)
- 响应格式：JSON 格式
- 跨域支持 (CORS)

#### 通用响应格式
```json
{
  "status": 0,       // 0 表示成功，1 表示失败
  "msg": "描述信息"   // 错误信息或其他描述
}
```

## 5. 前端模块设计

### 前端框架结构

#### Vue.js部分
- 使用Vue 3 Composition API
- 状态管理采用Pinia
- 路由管理采用Vue Router 4

#### React部分
- 使用React 18 Hooks
- 状态管理采用Redux Toolkit或Context API
- 路由管理采用React Router v6

### 组件结构
1. **公共组件** (components/)
    - Header.vue/Header.jsx - 页面头部
    - Footer.vue/Footer.jsx - 页面底部
    - ActivityCard.vue/ActivityCard.jsx - 活动卡片
    - Modal.vue/Modal.jsx - 弹窗组件
    - Button.vue/Button.jsx - 按钮组件（基于Ant Design Element）

2. **页面组件** (views/)
    - Login.vue/Login.jsx - 登录页面
    - Register.vue/Register.jsx - 注册页面
    - Home.vue/Home.jsx - 首页
    - ActivityList.vue/ActivityList.jsx - 活动列表页面
    - ActivityDetail.vue/ActivityDetail.jsx - 活动详情页面
    - Profile.vue/Profile.jsx - 用户个人中心

3. **路由配置** (router/)
    - routes.js - 路由映射配置
    - guards.js - 路由守卫

4. **状态管理** (store/)
    - user.js - 用户状态管理
    - activity.js - 活动状态管理
    - registration.js - 报名状态管理

5. **服务封装** (services/)
    - api.js - 基础API封装
    - authService.js - 认证相关API
    - activityService.js - 活动相关API
    - registrationService.js - 报名相关API

### UI设计规范

#### Ant Design Element组件使用
- 表单组件：a-form, a-input, a-select等
- 表格组件：a-table
- 按钮组件：a-button
- 弹窗组件：a-modal
- 导航组件：a-menu, a-breadcrumb

#### TailwindCSS样式规范
- 使用TailwindCSS工具类进行样式设计
- 遵循响应式设计原则
- 使用预定义颜色方案保持一致性
- 利用flexbox和grid进行布局

## 6. 数据库设计

### 用户表 (user)
- id (VARCHAR) - 用户ID
- username (VARCHAR) - 用户名
- password (VARCHAR) - 密码
- email (VARCHAR) - 邮箱
- avatar (VARCHAR) - 头像
- role (VARCHAR) - 角色 (user/admin)

### 活动表 (activity)
- id (VARCHAR) - 活动ID
- name (VARCHAR) - 活动名称
- description (VARCHAR) - 活动描述
- publisher_id (VARCHAR) - 发布者ID
- max_num (INT) - 最大人数
- event_time (VARCHAR) - 活动时间
- start_time (VARCHAR) - 开始时间
- end_time (VARCHAR) - 结束时间
- published_at (BIGINT) - 发布时间戳
- status (VARCHAR) - 状态 (pending/approved/rejected)

### 报名表 (registration)
- id (VARCHAR) - 报名ID
- user_id (VARCHAR) - 用户ID
- activity_id (VARCHAR) - 活动ID
- status (VARCHAR) - 状态 (已申请/已通过/已拒绝)

## 7. 功能模块划分

### 用户模块
- 用户注册/登录
- 用户信息管理
- 密码修改
- 权限管理（普通用户/管理员）

### 活动模块
- 活动发布
- 活动查询（列表/详情）
- 活动修改
- 活动删除
- 活动审核（管理员）

### 报名模块
- 活动报名
- 报名审核
- 报名查询
- 报名管理

## 8. 部署与运行

### 后端运行
```bash
# 编译Java代码
javac -cp "lib/*" src/**/*.java

# 运行服务器
java -cp "src;lib/*" Server
```

### 前端运行
```bash
# 安装依赖
npm install

# 开发环境运行
npm run dev

# 生产环境构建
npm run build
```

## 9. 开发规范

### 后端开发规范
- 遵循RESTful API设计原则
- 使用统一的响应格式
- 合理处理异常情况
- 保证数据一致性
- 添加必要的日志记录

### 前端开发规范
- 组件化开发模式
- 遵循Vue/React最佳实践
- 使用Ant Design Element组件库
- 应用TailwindCSS样式系统
- 统一的状态管理
- 合理的路由设计

## 10. 扩展性考虑

### 后端扩展
- 可集成Spring Boot框架提升开发效率
- 可替换为MySQL以外的数据库系统
- 可增加缓存层提升性能
- 可集成消息队列处理异步任务

### 前端扩展
- 可集成更多第三方UI组件库
- 可添加国际化支持
- 可增加PWA支持
- 可集成更多前端工具链
---
trigger: always_on
---

