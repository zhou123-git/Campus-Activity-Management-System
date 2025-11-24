# 项目架构文档

## 1. 项目概述

本项目是一个校园活动管理系统，采用前后端分离架构设计。后端使用Java原生HttpServer提供RESTful API接口，前端使用Vue 3框架结合Bootstrap、TailwindCSS样式框架构建现代化用户界面。

## 2. 技术栈

### 后端技术栈
- **编程语言**: Java
- **数据库**: MySQL
- **数据库连接**: JDBC
- **Web服务器**: 原生HttpServer (com.sun.net.httpserver)
- **项目构建**: 原生Java编译

### 前端技术栈
- **核心框架**: Vue 3
- **UI组件库**: Bootstrap
- **样式框架**: TailwindCSS
- **HTTP客户端**: Fetch API
- **构建工具**: Vite

## 3. 项目目录结构

```
D:\OOP\Campus-Activity-Management-System\
├── src/                       # 后端代码目录
│   ├── db/                    # 数据库连接配置
│   ├── entity/                # 实体类
│   ├── service/               # 业务逻辑层
│   ├── storage/               # 数据存储层
│   ├── Server.java            # HTTP服务器入口
│   └── App.java               # 控制台应用入口
├── frontend/                  # 前端代码目录
│   ├── index.html             # 主页面
│   ├── app-main.js            # Vue应用及主要逻辑
│   ├── vite.config.js         # Vite构建配置
│   └── tailwind.config.js     # TailwindCSS配置文件
├── resources/                 # 资源目录
│   └── db/                    # 数据库脚本
├── lib/                       # 外部依赖库
└── bin/                       # 编译输出目录
```

## 4. 后端模块设计

### 分层架构
采用简化的分层架构模式：

1. **HTTP层**（请求处理层）
   - Server.java - 处理所有HTTP请求和路由分发

2. **Service层**（业务逻辑层）
   - UserService.java - 用户业务逻辑
   - ActivityService.java - 活动业务逻辑
   - RegistrationService.java - 报名业务逻辑

3. **Entity层**（实体类）
   - User.java - 用户实体
   - Activity.java - 活动实体
   - Registration.java - 报名实体

4. **DB层**（数据库连接）
   - DB.java - 数据库连接配置

5. **Storage层**（数据存储层）
   - UserStorage.java - 用户数据存储
   - ActivityStorage.java - 活动数据存储
   - RegistrationStorage.java - 报名数据存储

### 统一接口规范

#### 接口约定
- 所有接口均以 `/api` 开头
- 使用 RESTful 风格设计
- 请求方法：POST (application/x-www-form-urlencoded)
- 响应格式：JSON 格式
- 跨域支持 (CORS)

#### 通用响应格式
``json
{
  "status": 0,      
  "msg": "描述信息"  
}
```

## 5. 前端模块设计

### 前端框架结构

#### Vue.js部分
- 使用Vue 3 Composition API
- 状态管理采用Vue响应式系统
- 路由管理采用手动视图切换

### 组件结构
1. **页面组件**
   - 登录/注册页面 - 处理用户认证
   - 活动列表页面 - 展示活动列表
   - 活动详情页面 - 展示活动详情
   - 个人中心页面 - 用户信息管理
   - 活动管理页面 - 管理员功能页面

2. **功能模块**
   - 用户认证模块 - 处理登录注册
   - 活动管理模块 - 处理活动的增删改查
   - 报名管理模块 - 处理活动报名和审核

### UI设计规范

#### Bootstrap组件使用
- 表单组件：form, input, select等
- 表格组件：table
- 按钮组件：button
- 导航组件：navbar, nav

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
javac -cp "lib/*" -d bin src/*.java src/service/*.java src/entity/*.java src/db/*.java

# 运行服务器
java -cp "bin;lib/*" Server
```

### 前端运行
```bash
# 使用Vite运行前端
npx vite
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
- 遵循Vue最佳实践
- 使用Bootstrap组件库
- 应用TailwindCSS样式系统
- 合理的状态管理
- 手动视图切换

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