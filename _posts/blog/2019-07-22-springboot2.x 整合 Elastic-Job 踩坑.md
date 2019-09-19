---
layout: post
title: springboot2.x 整合 Elastic-Job 踩坑
categories: Elastic-Job
description: Elastic-Job
keywords: Elastic-Job
---

> java.lang.ClassNotFoundException: org.apache.curator.connection. StandardConnectionHandlingPolicy

`springboot2.x` 整合 `Elastic-Job` 时会抛出一下异常

```java
Caused by: java.lang.ClassNotFoundException: org.apache.curator.connection.StandardConnectionHandlingPolicy
	at java.net.URLClassLoader.findClass(URLClassLoader.java:381) ~[na:1.8.0_172]
	at java.lang.ClassLoader.loadClass(ClassLoader.java:424) ~[na:1.8.0_172]
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349) ~[na:1.8.0_172]
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357) ~[na:1.8.0_172]
	... 40 common frames omitted
```

原因:
`Elastic-Job`项目基于开源产品`Quartz`和`Zookeeper`及其客户端`Curator`进行二次开发，如果与新版的`Spring Boot`一起使用，会出现`Curator`的版本冲突。


解决办法:在`pom.xml`中引入低版本的`Curator`依赖即可
```java
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>2.10.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>2.10.0</version>
</dependency>
```
