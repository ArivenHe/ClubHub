# 社团成长文档平台 (ClubHub)

基于你给的技术栈实现的前后端分离 MVP：
- 前端：Vue 3 + Vue Router（Options API）
- 后端：Spring Boot + JPA + MySQL + Sa-Token
- 权限：RBAC（用户-角色-权限）

## 1. 功能清单
- 成员登录
- 支持用户名或学号登录
- 前台关闭注册，仅支持后台管理员创建账号（手动/Excel）
- 全站浅色/深色模式切换
- 每人上传自己的成长总结文档（支持 Markdown 正文）
- 支持从本地导入 `.md/.markdown` 文件作为正文
- 文档正文支持 Markdown 编写、预览与本地导入
- 全站文档列表（成员端独立页面）
- 首页推荐文章展示（由后台设置推荐）
- 列表页仅展示标题/摘要，点击进入详情页查看正文与大纲导航
- 文档卡片整块可点击进入详情页
- 成员可删除自己的文章
- 成员可编辑自己的文章
- 文档支持选择标签（Tag）
- 普通用户也可在前台创建标签并使用
- 文章点赞（列表和详情均可点赞/取消点赞）
- 评论系统（详情页查看评论、发表评论、删除自己的评论）
- 用户端支持修改自己的登录密码
- 软件推荐区（成员端只读）
- 管理员 Excel 批量注册账号
- 后台用户管理（启用/禁用、改角色、重置密码）
- 后台用户管理支持筛选（关键词/状态/角色）
- 后台 RBAC 管理（角色/权限增删改查，角色可配置权限）
- 后台文章管理（筛选、发布/草稿切换、推荐切换、删除）
- 后台评论管理（按关键词/文章ID筛选，删除评论）
- 后台标签管理（增删改查，可查看标签创建人）
- 后台软件管理（新增推荐）
- 管理功能独立后台（`/admin`）
- Sa-Token + RBAC 接口权限控制

## 2. 目录结构
- `/backend` Spring Boot 后端
- `/frontend` Vue 前端
- `/docs/user-import-template.csv` Excel 导入模板（可另存为 xlsx）
- `/backend/sql/init_data.sql` 可选初始化软件推荐数据

## 3. 后端启动
1. 创建 MySQL 数据库：`clubhub`
2. 修改配置文件：`/backend/src/main/resources/application.yml`
   - `spring.datasource.url`
   - `spring.datasource.username`
   - `spring.datasource.password`
3. 进入后端目录并启动：
   - `cd /Users/ariven/1-jmiopenatom/experienceSharing/backend`
   - `mvn spring-boot:run`

说明：
- 首次启动会自动创建角色、权限、管理员账号。
- 默认管理员：`admin`
- 默认密码：`123456`（可通过 `app.default-password` 修改）
- 文档正文以 Markdown 文本方式存储。

## 4. 前端启动
1. 进入前端目录：
   - `cd /Users/ariven/1-jmiopenatom/experienceSharing/frontend`
2. 安装依赖：
   - `npm install`
3. 启动开发服务器：
   - `npm run dev`

可选环境变量：
- `VITE_API_BASE_URL`（默认 `http://localhost:8080`）
- `VITE_SA_TOKEN_NAME`（默认 `jmi-openatom`）

## 5. Excel 导入格式
第一行表头，数据从第二行开始，列顺序固定：
- 第 1 列：`username`
- 第 2 列：`realName`
- 第 3 列：`studentNo`（可空）

默认导入角色编码为 `MEMBER`，可在导入时指定其他角色编码。

## 6. 主要权限点
- `user:import` 批量导入用户
- `user:manage` 管理用户（角色/状态/密码）
- `rbac:manage` 管理角色与权限
- `doc:upload` 上传文档
- `doc:read` 查看文档
- `doc:manage` 后台管理文章
- `tag:read` 查看标签
- `tag:create` 创建标签
- `tag:manage` 管理标签
- `software:create` 新增软件推荐
- `software:read` 查看软件推荐

## 7. 推荐下一步
1. 增加文档审核流（例如提交/审核通过/驳回）。
2. 增加成员密码修改与重置。
3. 给文档与软件推荐加分页和搜索。
