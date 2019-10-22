---
layout: post
title: Spring系列十一：Spring @Configuration注解
categories: Spring
description: Spring
keywords: Spring
---

> 我欲穿花寻路，直入白云深处，浩气展虹霓。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring15.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring15.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring15.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring15.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring15.jpg")


## 概述

`Spring` `@Configuration`注解有助于基于`Spring`注解的自动装配。`@Configuration`注解指示一个类声明了一个或多个`@Bean`方法，`Spring`容器可以对该类进行处理，以便在运行时为这些`bean`生成`bean`定义和服务请求。。

从`spring 2`开始，我们就将`bean`配置写入`xml`文件中。但是`Spring 3`允许将`bean`定义移出`xml`文件。我们可以在`Java`文件本身中给出`bean`定义。这称为`Spring` `Java Config`功能（使用`@Configuration`注解）

### `Spring` `@Configuration`注解用法

在任何类顶部使用`@Configuration`注解来声明该类提供了一个或多个`@Bean`方法，并且可以由`Spring`容器进行处理以在运行时为这些`bean`生成`bean`定义和服务请求。

```java
@Configuration
public class AppConfig {
 
    @Bean(name="demoService")
    public DemoClass service()
    {
        
    }
}
```

### `Spring` `@Configuration`注解示例

[`@Configuration`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html)注解的用法。

#### 创建`maven`项目

```xml
mvn archetype:generate
    -DgroupId=cn.howtodoinjava.core
    -DartifactId=springCoreTest
    -DarchetypeArtifactId=maven-archetype-quickstart
    -DinteractiveMode=false
 
mvn eclipse:eclipse
```

#### 更新`Spring`依赖项

```xml
<properties>
	<spring.version>3.0.5.RELEASE</spring.version>
  </properties>
 
  <dependencies>
      
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
    
    <!-- Spring 3 dependencies -->
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-core</artifactId>
		<version>${spring.version}</version>
	</dependency>

	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-context</artifactId>
		<version>${spring.version}</version>
	</dependency>
	
	<!-- For JavaConfig -->
	<dependency>
		<groupId>cglib</groupId>
		<artifactId>cglib</artifactId>
		<version>2.2.2</version>
	</dependency>
	
  </dependencies>
```

#### 创建`spring` `bean`

```java
public interface DemoManager {
    public String getServiceName();
}
 
public class DemoManagerImpl implements DemoManager
{
    @Override
    public String getServiceName()
    {
        return "My first service with Spring 3";
    }
}
```

#### `Spring`配置类

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
import cn.howtodoinjava.core.beans.DemoManager;
import cn.howtodoinjava.core.beans.DemoManagerImpl;
 
@Configuration
public class ApplicationConfiguration {
 
    @Bean(name="demoService")
    public DemoManager helloWorld()
    {
        return new DemoManagerImpl();
    }
}
```

### 测试

测试`Java Config` 可配置的`bean`。

```java

package cn.howtodoinjava.core.verify;
 
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
import com.howtodoinjava.core.beans.DemoManager;
import com.howtodoinjava.core.config.ApplicationConfiguration;
 
public class VerifySpringCoreFeature
{
    public static void main(String[] args)
    {
        ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
 
        DemoManager  obj = (DemoManager) context.getBean("demoService");
 
        System.out.println( obj.getServiceName() );
    }
}
```

---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring @Configuration annotation example](https://howtodoinjava.com/spring-core/spring-configuration-annotation/)
