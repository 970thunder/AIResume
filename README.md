# AI 智能简历生成器

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

一份简历，无限可能。
本项目是一个功能强大的全栈AI简历生成器，旨在帮助用户快速、智能地创建专业、美观的个人简历。用户只需上传现有的简历文件（PDF, Word等格式），AI将自动提取、分析并结构化信息。随后，用户可以选择心仪的模板，通过强大的在线编辑器进行修改，一键生成精美简历，并支持高清PDF格式下载。

## ✨ 核心功能

*   **📄 多格式文件上传**: 支持 `.pdf`, `.docx`, `.txt` 等多种主流文件格式上传。
*   **🤖 智能信息提取**: 集成 **DeepSeek API** 对上传的简历内容进行深度分析，自动提取并填充至在线编辑器中，方便二次修改。
*   **👤 用户中心与简历管理**: 提供独立的用户中心，方便管理历史生成的简历，支持对简历进行重命名、重新编辑和删除。
*   **✍️ 所见即所得编辑器**: 强大的在线编辑器，左侧表单式填写，右侧实时预览简历效果，所有修改即时同步。
*   **🎨 丰富的模板选择**: 提供多种专业简历模板，所有模板内容均由后端动态渲染，加载速度更快。
*   **🚀 高清PDF导出**: 一键将最终生成的简历下载为高质量的PDF文件，方便投递和打印。

## 🛠️ 技术栈

本项目采用前后端分离的架构：

*   **前端 (UI)**
    *   **框架**: [Vue 3](https://vuejs.org/)
    *   **构建工具**: [Vite](https://vitejs.dev/)
    *   **UI组件库**: [Element Plus](https://element-plus.org/)
    *   **状态管理**: [Pinia](https://pinia.vuejs.org/)
    *   **HTTP请求**: [Axios](https://axios-http.com/)
    *   **PDF生成**: [html2pdf.js](https://github.com/eKoopmans/html2pdf.js)
    *   **动画**: [@vueuse/motion](https://motion.vueuse.org/)

*   **后端 (Back)**
    *   **框架**: [Spring Boot 3](https://spring.io/projects/spring-boot)
    *   **语言**: Java 21
    *   **数据持久化**: Spring Data JPA / Hibernate
    *   **安全与认证**: Spring Security
    *   **数据库**: MySQL
    *   **AI服务**: [DeepSeek API](https://platform.deepseek.com/)

## 📁 项目结构

```
.
├── Back/         # 后端 Spring Boot 项目
│   ├── src/
│   └── pom.xml
├── UI/           # 前端 Vue 项目
│   ├── public/
│   ├── src/
│   └── package.json
└── README.md
```

## 🚀 快速开始

请确保您的本地环境已安装以下软件：

*   Java 21
*   Maven 3.x
*   Node.js v18.x 或更高版本
*   MySQL 8.x

### 1. 数据库配置

1.  在您的MySQL服务中创建一个新的数据库，例如 `resume_generator`。
2.  **重要**: 建议将 `application.yml` 中的 `spring.jpa.hibernate.ddl-auto` 设置为 `update`，以便程序启动时自动根据实体类更新表结构。如果需要手动创建，除了执行 `Back/src/main/resources/schema.sql` 文件外，还需手动为 `resumes` 和 `templates` 表添加新字段：
    ```sql
    ALTER TABLE resumes ADD COLUMN title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ALTER TABLE templates ADD COLUMN html_content LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
3.  **重要**: 向 `templates` 表中插入模板数据。请注意，`html_content` 字段需要包含完整的HTML代码。
    ```sql
    -- 由于html_content内容过长，此处仅为示例。请确保将真实的HTML模板内容插入。
    INSERT INTO `templates` (`id`, `name`, `type`, `description`, `price`, `template_path`, `html_content`) VALUES
    (1, '经典商务', 'free', '适合传统行业和商务场合', NULL, '/templates/classic.html', '<!DOCTYPE html>...'),
    (2, '现代简约', 'free', '简洁现代，适合各种职位', NULL, '/templates/modern.html', '<!DOCTYPE html>...'),
    (6, '学术研究', 'free', '适合学术界和研究人员', NULL, '/templates/academic.html', '<!DOCTYPE html>...');
    ```

### 2. 后端启动

1.  打开 `Back/src/main/resources/application.yml` 文件。
2.  修改 `spring.datasource` 部分，填入您自己的数据库 `url`, `username`, 和 `password`。
3.  修改 `deepseek` 部分，填入您自己的 `api-key`。
4.  在项目根目录 `Back/` 下运行命令：
    ```bash
    mvn clean spring-boot:run
    ```
5.  后端服务将启动在 `http://localhost:9090`。

### 3. 前端启动

1.  进入前端项目目录：
    ```bash
    cd UI
    ```
2.  安装项目依赖：
    ```bash
    npm install
    ```
3.  启动开发服务器：
    ```bash
    npm run dev
    ```
4.  前端服务将启动在 `http://localhost:5173` (或终端提示的其他端口)。在浏览器中打开该地址即可访问。

## 📝 API 接口

| 方法 | URL | 权限 | 描述 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | 用户注册 |
| `POST` | `/api/auth/login` | Public | 用户登录 |
| `GET` | `/api/templates` | Public | 获取所有可用模板 |
| `POST` | `/api/files/upload` | Authenticated | 上传简历文件以供AI分析 |
| `POST` | `/api/resume/generate` | Authenticated | 基于上传的文件和所选模板生成新简历 |
| `GET` | `/api/resumes` | Authenticated | 获取当前用户的所有简历历史 |
| `GET` | `/api/resume/{id}` | Authenticated | 获取单个简历的详细信息（用于编辑） |
| `PUT` | `/api/resume/{id}` | Authenticated | 保存对简历内容的修改 |
| `PUT` | `/api/resume/{id}/title` | Authenticated | 修改简历标题 |
| `DELETE` | `/api/resume/{id}` | Authenticated | 删除指定的简历 |


## 📄 开源许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT) 开源许可证。
