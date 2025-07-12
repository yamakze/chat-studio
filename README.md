# 💬 Chat-Studio

> 基于 Spring AI + Vue 2 开发的本地 AI 对话工作室，支持多模型、多功能协作。


## 📖 项目简介（Introduction）

**Chat-Studio** 是一个面向个人开发者与 AI 爱好者的多模型对话客户端。  
项目支持模型接入管理、知识增强、文件管理、系统角色配置、上下文记忆机制等核心功能，  
全部数据本地存储，配备灵活的后台管理页面，帮助用户定制专属的智能对话体验。


## 🛠️ 技术栈（Tech Stack）

| 后端                           | 前端              | 数据库                 | 其他           | 开发环境              |
|------------------------------|-----------------|---------------------|--------------|-------------------|
| Spring AI 1.0.0、MyBatis-Plus | Vue 2、ElementUI | MySQL 、 PostgresSQL | Docker、Maven | JDK 17、MacOS 15.3 |

---

## 🧠 核心设计（Core Architecture）

![核心设计](/img/核心设计.png)


![流程图](/img/流程图.png)

---

## 🚀 项目特性（Features）

- 🧠 **记忆管理系统**：支持对历史对话的删除、重试、重提问、导出；后台定时分析形成长期记忆，实现个性化聊天。
- 📚 **知识库增强**：支持上传本地文件或解析代码仓库 URL，构建专属知识体系。
- 📎 **文件对话支持**：用户可上传附件，通过 `@file:` 指令引用，便捷嵌入上下文。
- 🔧 **模型调用与 MCP 接入**：兼容 MCP 协议，只需简单配置即可实现模型多任务能力。
- ⚙️ **模型配置灵活热更新**：支持系统角色、聊天上下文长度、模型 Options 等在线热切换。
- 🤖 **扩展特性开发中**：支持 Agent 执行模式、文生图、多客户端管理等（敬请期待）。

---

## 🖼️ 效果展示（Screenshots）

### 💬 对话交互页面

![对话页面](/img/前端页面01.png)
![文件列表](/img/前端页面02.png)

<details>
<summary>📷 更多</summary>

<p>
  <img src="/img/前端页面03.png" style="width:32%;margin:0.5%" />
  <img src="/img/前端页面04.png" style="width:32%;margin:0.5%" />
  <img src="/img/前端页面13.png" style="width:32%;margin:0.5%" />
</p>

</details>

---

### 🛠️ 后台管理面板

![客户端管理](/img/前端页面05.png)
![客户端管理](/img/前端页面06.png)

<details>
<summary>📂 查看全部</summary>

<p>
  <img src="/img/前端页面07.png" style="width:32%;margin:0.5%" />
  <img src="/img/前端页面08.png" style="width:32%;margin:0.5%" />
  <img src="/img/前端页面09.png" style="width:32%;margin:0.5%" />
</p>
<p>
  <img src="/img/前端页面10.png" style="width:32%;margin:0.5%" />
  <img src="/img/前端页面11.png" style="width:32%;margin:0.5%" />
  <img src="/img/前端页面12.png" style="width:32%;margin:0.5%" />
</p>

</details>

---

## 📦 安装与运行（Installation & Run）

### 🔗 克隆项目

```bash
git clone https://github.com/yamakze/chat-studio.git
````

### ☕ 启动后端服务

```bash
cd chat-studio
bash start.sh
```

> 🔧 默认运行端口：`http://localhost:8091`

📌 默认配置文件路径：`src/main/resources/application.yml`，请根据实际情况配置数据库连接、模型参数等信息。

### 💻 启动前端服务
```bash
cd chat-studio
npm install -g serve
serve -s front
open "http://localhost:3000"
```


---

## 📬 联系与反馈（Feedback）

欢迎Star、Fork 项目！

* GitHub: [@yamakze](https://github.com/yamakze)
* Gitee: [@hermione-yamada](https://gitee.com/hermione-yamada)


