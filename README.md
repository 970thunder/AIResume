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
│   │       ├── application.yml # 核心配置文件
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

## ⚙️ 环境配置

### 后端
在 `Back/src/main/resources/application.yml` 文件中，你需要配置以下信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resume_generator?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: YOUR_DATABASE_USERNAME
    password: YOUR_DATABASE_PASSWORD
  jpa:
    hibernate:
      ddl-auto: update # 在开发环境中建议使用update，会自动同步表结构

# DeepSeek API Key
deepseek:
  api-key: "YOUR_DEEPSEEK_API_KEY"
```

### 前端
前端项目通过 Vite 的代理功能与后端进行通信，通常无需单独配置。请确保 `vite.config.js` 文件中的代理目标地址与你的后端服务地址一致。

```javascript
// UI/vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:9090', // 确认此地址为你的后端地址
      changeOrigin: true,
    }
  }
}
```

## 🚀 快速开始

请确保您的本地环境已安装以下软件：
*   Java 21
*   Maven 3.x
*   Node.js v18.x 或更高版本
*   MySQL 8.x

### 1. 数据库配置
1.  在您的MySQL服务中创建一个新的数据库，例如 `resume_generator`。
2.  **重要**: 建议将 `application.yml` 中的 `spring.jpa.hibernate.ddl-auto` 设置为 `update`。这将使程序启动时自动根据实体类更新表结构，无需手动执行SQL。
3.  **如果需要手动初始化**: 请向 `templates` 表中插入模板数据。注意，`html_content` 字段需要包含完整的HTML代码。
    ```sql
    -- 由于html_content内容过长，此处仅为示例。请确保将真实的HTML模板内容插入。
    INSERT INTO `templates` (`id`, `name`, `type`, `description`, `price`, `template_path`, `html_content`) VALUES
    (1, '经典商务', 'free', '适合传统行业和商务场合', NULL, '/templates/classic.html', '<!DOCTYPE html>...'),
    (2, '现代简约', 'free', '简洁现代，适合各种职位', NULL, '/templates/modern.html', '<!DOCTYPE html>...'),
    (6, '学术研究', 'free', '适合学术界和研究人员', NULL, '/templates/academic.html', '<!DOCTYPE html>...');
    ```

### 2. 后端启动
1.  检查并完成 **环境配置** 部分的 `application.yml` 设置。
2.  在项目根目录 `Back/` 下运行命令：
    ```bash
    mvn clean spring-boot:run
    ```
3.  后端服务将启动在 `http://localhost:9090`。

### 3. 前端启动
1.  进入前端项目目录： `cd UI`
2.  安装项目依赖： `npm install`
3.  启动开发服务器： `npm run dev`
4.  前端服务将启动在 `http://localhost:5173` (或终端提示的其他端口)。在浏览器中打开该地址即可访问。


## 📦 部署指南

### 后端
1.  在 `Back/` 目录下执行Maven打包命令：
    ```bash
    mvn clean package -DskipTests
    ```
2.  打包完成后，会在 `Back/target/` 目录下生成一个 `.jar` 文件（例如 `generator-0.0.1-SNAPSHOT.jar`）。
3.  将此 `jar` 文件上传到您的服务器，并使用java命令运行它：
    ```bash
    java -jar generator-0.0.1-SNAPSHOT.jar
    ```

### 前端
1.  在 `UI/` 目录下执行打包命令：
    ```bash
    npm run build
    ```
2.  打包完成后，会在 `UI/` 目录下生成一个 `dist` 文件夹。
3.  将 `dist` 文件夹内的所有内容部署到您的Web服务器（如 Nginx）的网站根目录。
4.  **重要**: 配置Nginx反向代理，将所有 `/api` 的请求转发到您后端服务的地址。示例配置如下：
    ```nginx
    location /api/ {
        proxy_pass http://localhost:9090/; # 后端服务地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    ```


## 📝 API 接口

| 方法 | URL | 权限 | 描述 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | 接收用户名、邮箱、密码进行注册 |
| `POST` | `/api/auth/login` | Public | 接收用户名和密码，成功后返回JWT |
| `GET` | `/api/templates` | Public | 获取所有可用模板的列表 |
| `POST` | `/api/files/upload` | Authenticated | 上传简历文件，返回文件ID供后续分析 |
| `POST` | `/api/resume/generate` | Authenticated | 基于文件ID和模板ID，由AI生成新简历 |
| `GET` | `/api/resumes` | Authenticated | 获取当前认证用户的所有简历历史 |
| `GET` | `/api/resume/{id}` | Authenticated | 获取单个简历的详细信息，用于编辑器加载 |
| `PUT` | `/api/resume/{id}` | Authenticated | 接收简历完整JSON数据，保存修改 |
| `PUT` | `/api/resume/{id}/title`| Authenticated | 仅修改指定简历的标题 |
| `DELETE`| `/api/resume/{id}` | Authenticated | 删除指定的简历 |


## 📄 开源许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT) 开源许可证。
