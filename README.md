# AI 智能简历生成器

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

一份简历，无限可能。

本项目是一个功能强大、体验友好的全栈AI简历生成器，旨在帮助用户通过AI技术，快速、智能地创建专业、美观的个人简历。用户只需上传现有的简历文件，AI将自动提取关键信息并填充至在线编辑器中。随后，用户可以选择心仪的模板，通过所见即所得的编辑器进行二次创作，最终一键生成精美简历并下载为高清PDF。

## 📷 项目截图
![项目截图](https://img.picui.cn/free/2025/07/08/686c0892b02b7.jpg)

## ✨ 核心功能详解

*   **👤 统一认证与用户中心**
    *   基于 Spring Security 和 JWT 实现安全的用户注册与登录。
    *   提供独立的用户中心，集中管理个人信息和所有历史简历。

*   **📄 AI驱动的简历解析**
    *   支持 `.pdf`, `.docx`, `.txt` 等多种主流文件格式。
    *   集成 **DeepSeek API** 对上传的简历内容进行深度分析，自动提取并结构化个人信息、工作经历、教育背景、技能等关键模块。

*   **✍️ 所见即所得的实时编辑器**
    *   强大的在线编辑功能，采用左侧表单、右侧实时预览的经典布局。
    *   所有信息的修改（包括个人信息、项目经历的增删改查）都会即时、无延迟地同步到右侧的预览视图中。
![1.png](https://img.picui.cn/free/2025/07/08/686c0a3b59f40.png)
*   **🎨 动态渲染的模板商城**
    *   提供多种专业简历模板，满足不同行业和岗位的需求。
    *   模板的HTML内容完全由后端动态加载并注入，前端负责渲染数据，极大提升了预览加载速度和灵活性。
    *   支持简历历史记录与指定模板进行关联，方便重新编辑。
![项目截图](https://img.picui.cn/free/2025/07/08/686c093f7e676.png)
*   **🚀 高清PDF导出与智能命名**
    *   一键将最终生成的简历下载为高质量的PDF文件，方便投递和打印。
    *   后端具备智能命名功能，在创建新简历时会自动处理重名情况（例如，“未命名简历”，“未命名简历 (1)”）。

## 🛠️ 技术栈与选型说明

### 前端 (UI)
*   **框架**: [Vue 3](https://vuejs.org/)
*   **构建工具**: [Vite](https://vitejs.dev/)
*   **UI组件库**: [Element Plus](https://element-plus.org/)
*   **状态管理**: [Pinia](https://pinia.vuejs.org/)
*   **HTTP请求**: [Axios](https://axios-http.com/)
*   **PDF生成**: [html2pdf.js](https://github.com/eKoopmans/html2pdf.js)
*   **动画**: [@vueuse/motion](https://motion.vueuse.org/)

### 后端 (Back)
*   **框架**: [Spring Boot 3](https://spring.io/projects/spring-boot)
*   **语言**: Java 21
*   **数据持久化**: Spring Data JPA / Hibernate
*   **安全与认证**: Spring Security (JWT)
*   **数据库**: MySQL
*   **AI服务**: [DeepSeek API](https://platform.deepseek.com/)

### 选型说明
*   **Vue 3 & Vite**: 提供了卓越的开发体验和性能，组合式API（Composition API）使得复杂逻辑的管理更加清晰。
*   **Pinia**: 作为Vue的官方推荐状态管理库，它设计简洁、类型安全，与Vue DevTools的集成度极高。
*   **Spring Boot & Spring Security**: Spring生态提供了稳定、成熟的全方位企业级开发支持。Spring Security为项目提供了强大且易于扩展的认证和授权框架。
*   **JPA/Hibernate**: 极大地简化了数据库操作，让开发者可以更专注于业务逻辑而非SQL语句。

## 📁 项目结构
```
.
├── Back/                 # 后端 Spring Boot 项目
│   ├── src/main
│   │   ├── java/com/resume/generator
│   │   │   ├── config      # Spring Security, CORS等配置
│   │   │   ├── controller  # API接口控制器
│   │   │   ├── dto         # 数据传输对象
│   │   │   ├── entity      # JPA实体类
│   │   │   ├── repository  # 数据访问层
│   │   │   └── service     # 业务逻辑层
│   │   └── resources
│   │       ├── application.yml # 核心配置文件 (需从example复制)
│   │       └── schema.sql      # 数据库初始化脚本
│   └── pom.xml
├── UI/                   # 前端 Vue 项目
│   ├── src
│   │   ├── api           # API请求封装
│   │   ├── assets        # 静态资源 (CSS, images)
│   │   ├── components    # 可复用Vue组件
│   │   ├── layouts       # 整体布局组件
│   │   ├── router        # 路由配置
│   │   ├── stores        # Pinia状态管理
│   │   └── views         # 页面级组件
│   └── package.json
└── README.md
```

## 🚀 快速开始 (Quick Start)

请确保您的本地环境已安装以下软件：
*   Java 21
*   Maven 3.x
*   Node.js v18.x 或更高版本
*   MySQL 8.x

### 1. 后端配置与启动 (Back)

#### 1.1 数据库准备
在您的 MySQL 服务中创建一个新的数据库，例如 `resume_generator`。
```sql
CREATE DATABASE resume_generator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 1.2 配置文件设置
1.  进入后端资源目录：`Back/src/main/resources/`
2.  **复制** `application.yml.example` 文件并重命名为 `application.yml`。
3.  打开 `application.yml`，修改以下关键配置：

```yaml
spring:
  datasource:
    # 修改数据库地址、用户名和密码
    url: jdbc:mysql://localhost:3306/resume_generator?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: YOUR_DB_USERNAME  # 例如 root
    password: YOUR_DB_PASSWORD  # 例如 123456

  mail:
    # 邮箱服务配置 (用于发送验证码等)
    host: smtp.qq.com
    username: your_email@qq.com
    password: your_smtp_password # 注意：通常是邮箱的授权码，而非登录密码

deepseek:
  # 配置 DeepSeek API Key (用于简历分析)
  api-key: your_deepseek_api_key

application:
  security:
    jwt:
      # 设置一个安全的密钥字符串
      secret-key: your_jwt_secret_key_change_this_to_something_secure
```

#### 1.3 启动后端
在项目根目录 `Back/` 下运行 Maven 命令：
```bash
mvn clean spring-boot:run
```
或者在 IDE (如 IntelliJ IDEA) 中直接运行 `ResumeGeneratorApplication` 类。

后端服务启动成功后，默认运行在 `http://localhost:9090`。

### 2. 前端配置与启动 (UI)

#### 2.1 安装依赖
进入前端项目目录并安装依赖：
```bash
cd UI
npm install
```

#### 2.2 启动开发服务器
```bash
npm run dev
```

前端服务将启动在 `http://localhost:5173` (或其他端口，视终端提示而定)。
在浏览器中打开该地址即可访问应用。

*注意：前端默认配置了代理 (`vite.config.js`) 将 `/api` 请求转发到 `http://localhost:9090`。如果您的后端端口不同，请同步修改 `vite.config.js`。*

## 📦 部署指南

### 后端部署
1.  在 `Back/` 目录下打包：
    ```bash
    mvn clean package -DskipTests
    ```
2.  生成的 `.jar` 文件位于 `Back/target/` 目录。
3.  服务器运行：
    ```bash
    java -jar generator-0.0.1-SNAPSHOT.jar
    ```

### 前端部署
1.  在 `UI/` 目录下构建：
    ```bash
    npm run build
    ```
2.  构建产物位于 `UI/dist/` 目录。
3.  将 `dist` 目录部署到 Nginx 或其他 Web 服务器。
4.  **Nginx 配置示例** (确保 `/api` 转发正确)：
    ```nginx
    server {
        listen 80;
        server_name your_domain.com;

        location / {
            root /path/to/dist;
            index index.html;
            try_files $uri $uri/ /index.html; # 支持 Vue Router history 模式
        }

        location /api/ {
            proxy_pass http://localhost:9090/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
    ```

## 📝 API 接口概览

| 方法 | URL | 权限 | 描述 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | 用户注册 |
| `POST` | `/api/auth/login` | Public | 用户登录 |
| `POST` | `/api/files/upload` | Auth | 上传简历文件 |
| `POST` | `/api/resume/generate` | Auth | AI生成简历 |
| `GET` | `/api/resumes` | Auth | 获取简历列表 |
