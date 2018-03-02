---
layout: post
title: Spring Boot 2.0 动画Banner
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---
> `Spring Boot`是由Pivotal团队提供的全新框架，其设计目的是用来简化新Spring应用的初始搭建以及开发过程。[v2.0.0.RELEASE](https://github.com/spring-projects/spring-boot/releases/tag/v2.0.0.RELEASE)已于昨天正式发布。

## 前言
本篇文章介绍`Spring Boot 2.0`一个有趣的功能`动画Banner`;(当然，在实际开发中没有用处，`just for fun`)

### 准备
- JDK 1.8 或更高版本
- Maven 3 或更高版本

### 技术栈
- Spring Boot 2.0

### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot01.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot01.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot01.png")
### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>cn.merryyou</groupId>
	<artifactId>animated-banner</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>animated-banner</name>
	<description>A very useful project to demonstrate animated gif support in Spring Boot 2</description>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.0.0.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<includeSystemScope>true</includeSystemScope>
				</configuration>
			</plugin>
		</plugins>
	</build>


</project>

```
### 项目打包

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.png")

### 控制台启动项目（MAC）

`java -jar animated-banner-0.0.1-SNAPSHOT.jar`

效果如下：
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot-banners.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot-banners.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot-banners.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot-banners.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot-banners.gif")



## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/animated-banner](https://github.com/longfeizheng/animated-banner)

---
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？