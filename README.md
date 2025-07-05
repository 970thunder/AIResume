# AI 智能简历生成器

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

一份简历，无限可能。本项目是一个功能强大的全栈AI简历生成器，旨在帮助用户快速、智能地创建专业、美观的个人简历。用户只需上传现有的简历文件（PDF, Word等格式），AI将自动提取、分析并结构化信息。随后，用户可以选择心仪的模板，一键生成精美简历，并支持高清PDF格式下载。

## ✨ 核心功能

*   **📄 多格式文件上传**: 支持 `.pdf`, `.docx`, `.txt` 等多种主流文件格式上传。
*   **🤖 智能信息提取**: 集成 **DeepSeek API** 对上传的简历内容进行深度分析，自动提取并填充个人信息、工作经历、教育背景、技能等关键模块。
*   **🎨 丰富的模板选择**: 提供多种专业简历模板（包括免费和付费），满足不同行业和岗位的需求。
*   **✏️ 实时编辑与预览**: 在AI分析后，用户可以对提取出的信息进行微调，并实时预览选择不同模板的最终效果。
*   **🚀 高清PDF导出**: 一键将最终生成的简历下载为高质量的PDF文件，方便投递和打印。

## 🛠️ 技术栈

本项目采用前后端分离的架构：

*   **前端 (UI)**
    *   **框架**: [Vue 3](https://vuejs.org/)
    *   **构建工具**: [Vite](https://vitejs.dev/)
    *   **UI组件库**: [Element Plus](https://element-plus.org/)
    *   **HTTP请求**: [Axios](https://axios-http.com/)
    *   **PDF生成**: [html2pdf.js](https://github.com/eKoopmans/html2pdf.js)

*   **后端 (Back)**
    *   **框架**: [Spring Boot 3](https://spring.io/projects/spring-boot)
    *   **语言**: Java 21
    *   **数据持久化**: Spring Data JPA / Hibernate
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
2.  进入数据库，执行 `Back/src/main/resources/schema.sql` 文件以创建所需的表结构。
3.  **重要**: 向 `templates` 表中插入模板数据。您可以参考以下SQL语句，确保 `id` 与前端代码 `UI/src/views/ResumeGenerator.vue` 中的定义一致：
    ```sql
    INSERT INTO templates (id, name, type, description, price, template_path) VALUES
    (1, '经典商务', 'free', '适合传统行业和商务场合', NULL, '/templates/classic.html'),
    (2, '现代简约', 'free', '简洁现代，适合各种职位', NULL, '/templates/modern.html'),
    (3, '创意设计', 'premium', '适合设计师和创意工作者', '¥29', ''),
    (4, '技术专业', 'premium', '专为技术人员优化', '¥35', ''),
    (5, '高端商务', 'premium', '高级管理层专用模板', '¥45', ''),
    (6, '学术研究', 'free', '适合学术界和研究人员', NULL, '/templates/academic.html');
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

| 方法 | URL | 描述 |
| :--- | :--- | :--- |
| `POST` | `/api/files/upload` | 上传简历文件 |
| `POST` | `/api/resume/analyze` | 使用AI分析简历内容 |
| `POST` | `/api/resume/generate` | 选择模板并生成最终简历 |

## 📄 开源许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT) 开源许可证。
