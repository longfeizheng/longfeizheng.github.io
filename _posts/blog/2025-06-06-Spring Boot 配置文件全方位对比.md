---
layout: post
title: application.properties vs. application.yml vs. bootstrap.yml：Spring Boot 配置文件全方位对比
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---

在 Spring Boot 应用中，配置管理是核心功能之一。`application.properties`、`application.yml` 和 `bootstrap.yml` 是三个最关键的配置文件，它们在语法、加载时机和用途上存在显著差异。理解这些差异对于构建健壮、可配置的应用程序至关G重要。

---
## `application.properties` vs. `application.yml`

这两个文件是 Spring Boot 最主要的本地配置文件，用于定义应用程序的绝大多数行为。它们的主要区别在于语法和格式。

### 语法差异

* **`application.properties`**
    使用传统的 **键值对** 格式，通过 `.` 来分隔层级关系。这种格式简单直接，历史悠久。

    **配置示例 (`.properties`)**
    ```properties
    # 服务器端口
    server.port=8080

    # 数据库配置
    spring.datasource.url=jdbc:mysql://localhost:3306/mydb
    spring.datasource.username=root
    spring.datasource.password=secret

    # 自定义层级配置
    myapp.notification.email.from=admin@example.com
    myapp.notification.email.subject=Hello
    ```

* **`application.yml` (.yaml)**
    使用 **YAML (YAML Ain't Markup Language)** 格式，通过缩进和冒号来表示层级结构。这种格式更具可读性，尤其在处理复杂和嵌套的配置时，能有效减少冗余。

    **配置示例 (`.yml`)**
    ```yaml
    # 服务器端口
    server:
      port: 8080

    # 数据库配置
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/mydb
        username: root
        password: secret

    # 自定义层级配置
    myapp:
      notification:
        email:
          from: admin@example.com
          subject: Hello
    ```

### 主要差异和选择

* **可读性:** 对于复杂的配置，`.yml` 的层级结构通常比 `.properties` 的长键名更清晰易读。
* **功能:** 两者功能完全相同，可以定义列表、Map 等复杂结构。
* **优先级:** 如果在同一目录下同时存在 `application.properties` 和 `application.yml`，**`.properties` 文件的优先级更高**。Spring Boot 会优先加载 `.properties` 文件中的配置。
* **选择建议:** 对于新项目，推荐使用 `.yml`，因为它更现代且可读性更好。如果团队习惯或项目历史悠久，使用 `.properties` 也完全没有问题。

---
## `bootstrap.yml` vs. `application.yml`

这是理解 Spring Boot 和 Spring Cloud 配置的关键。它们的核心区别在于**加载时机和用途**。

### 加载顺序 🚀

1.  **Bootstrap 阶段:** `bootstrap.yml` (或 `bootstrap.properties`) 首先被加载。
2.  **Application 阶段:** `application.yml` (或 `application.properties`) 在之后被加载。

`bootstrap.yml` 由一个特殊的 "bootstrap context" 加载，这个上下文是主 "application context" 的父上下文。

### 用途差异

* **`bootstrap.yml`**
    **主要用于 Spring Cloud 环境**。它的核心使命是在应用程序主上下文启动**之前**，配置好一些引导性的、至关重要的参数。这些参数通常用于从外部源加载配置。

    **典型用途:**
    * **配置中心连接:** 指定 Spring Cloud Config Server 的地址、分支、应用名等，以便应用启动时能从远端拉取配置。
    * **服务发现与注册:** 配置 Eureka、Consul 等服务发现客户端的早期参数。
    * **加密解密:** 配置加密后端（如 Vault）的密钥，以便解密远端配置中的密文。

    **配置示例 (`bootstrap.yml`)**
    ```yaml
    spring:
      application:
        # 应用名，用于从配置中心拉取配置
        name: my-awesome-service
      cloud:
        config:
          # 配置中心的地址
          uri: http://config-server:8888
          # 使用的 profile
          profile: prod
          # 使用的分支
          label: main
    ```

* **`application.yml`**
    用于定义**应用程序自身**的配置，是应用的主配置文件。它包含了绝大部分的业务配置、数据源、服务器行为等。这些配置在 `bootstrap.yml` 加载完毕后，由主应用程序上下文加载。

### 核心区别总结

| 特性 | `bootstrap.yml` / `.properties` | `application.yml` / `.properties` |
| :--- | :--- | :--- |
| **加载时机** | **非常早**，在主应用上下文启动前 | **较晚**，在 bootstrap 过程后 |
| **加载上下文** | Bootstrap Context (父上下文) | Application Context (子上下文) |
| **主要用途** | 配置外部化配置源 (如 Spring Cloud Config) | 定义应用程序自身的本地配置 |
| **依赖关系** | 通常需要 `spring-cloud-starter-bootstrap` 依赖 | Spring Boot 默认支持 |
| **属性优先级** | **优先级高**。默认情况下，`bootstrap` 阶段加载的属性（包括远端配置）不能被本地 `application` 配置覆盖。 | **优先级低**。 |

---
## 总结与选择建议

1.  **`.properties` vs. `.yml`?**
    * 这是一个**语法风格**的选择。推荐使用 `.yml`，因为它更具可读性。

2.  **需要 `bootstrap.yml` 吗?**
    * **如果你不使用 Spring Cloud Config** 或其他需要在主应用启动前就准备好的外部配置系统，那么你**完全不需要 `bootstrap.yml`**。
    * **如果你正在使用 Spring Cloud Config**、Consul Config 或 Vault 等，那么 `bootstrap.yml` 是必需的，用于存放连接这些外部系统的“引导”配置。

简单来说，**绝大多数标准 Spring Boot 应用只需要 `application.yml` (或 `.properties`)**。只有在引入了需要早期配置的 Spring Cloud 组件时，才需要创建和使用 `bootstrap.yml`。
