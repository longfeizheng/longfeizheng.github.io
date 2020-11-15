---
layout: post
title: Spring Boot – 注解
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---

>一场寂寞凭谁诉。算前言，总轻负。

a

## 概述 ##

`spring boot`注解大部分是在`org.springframework.boot.autoconfigure`和`org.springframework.boot.autoconfigure.condition`包中。
让我们来了解一些常用的`spring boot`注解以及它们背后的工作原理。

### `@SpringBootApplication`

`Spring boot`主要功能是自动配置。这种自动配置是通过组件扫描完成的，即在`classspath`中为`@Component`注解找到所有类。它还涉及到扫描`@Configuration`注解和初始化一些额外的`bean`。

`@SpringBootApplication`注解可一步实现所有功能。它启用了三个功能：

1. `@EnableAutoConfiguration`：启用自动配置机制
2. `@ComponentScan`：启用`@Component`扫描
3. `@SpringBootConfiguration`：在上下文中注册额外的`bean`

>用`@SpringBootApplication`注解的`Java`类是`Spring Boot`应用程序的主类，应用程序从此处开始。

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
 
@SpringBootApplication
public class Application {
 
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
 
}

```

### `@EnableAutoConfiguration`

该注解支持`Spring`应用程序上下文的自动配置，尝试根据类路径中预定义类的存在来猜测和配置我们可能需要的`bean`。

例如，如果在类路径上有`tomcat-embedded.jar`，则可能需要`TomcatServletWebServerFactory`。

由于这个注解已经包含在`@SpringBootApplication`中，所以在主类上再次添加它没有影响。还建议使用`@SpringBootApplication`。

自动配置类是一些常规的`Spring` 配置 `bean`。它们使用`SpringFactoriesLoader`机制定位。通常，自动配置`bean`是`@Conditional` `Bean`（最经常使用`@ConditionalOnClass`和`@ConditionalOnMissingBean`注解）。

### ` @SpringBootConfiguration`

它表明类提供了`Spring boot`应用程序配置。它可以作为`Spring`的标准`@Configuration`注解的替代，这样就可以自动找到配置。

应用程序应该只包含一个`@SpringBootConfiguration`，大多数惯用的`Spring Boot`应用程序将从`@SpringBootApplication`继承它。

这两个注解的主要区别是`@SpringBootConfiguration`允许自动定位配置。这对于单元测试或集成测试特别有用。

### `@ImportAutoConfiguration`

它只导入和应用指定的自动配置类。`@ImportAutoConfiguration`和`@EnableAutoConfiguration`之间的区别在于，后者尝试在扫描期间配置类路径中找到的`bean`，而`@ImportAutoConfiguration`只运行我们在注解中提供的配置类。

当我们不想启用默认的自动配置时，应使用`@ImportAutoConfiguration`。

```java
@ComponentScan("path.to.your.controllers")
@ImportAutoConfiguration({WebMvcAutoConfiguration.class
    ,DispatcherServletAutoConfiguration.class
    ,EmbeddedServletContainerAutoConfiguration.class
    ,ServerPropertiesAutoConfiguration.class
    ,HttpMessageConvertersAutoConfiguration.class})
public class App 
{
    public static void main(String[] args) 
    {
        SpringApplication.run(App.class, args);
    }
}
```

### `@AutoConfigureBefore`, `@AutoConfigureAfter`, `@AutoConfigureOrder`

如果我们的配置需要以特定顺序（在应用之前或者之后），则可以使用`@AutoConfigureAfter`或`@AutoConfigureBefore`批注。

如果我们想要排序某些毫无关系的自动配置，我们还可以使用`@AutoConfigureOrder`。该注解与常规的`@Order`注解具有相同的语义，但为自动配置类提供了专用的顺序。

```java
@Configuration
@AutoConfigureAfter(CacheAutoConfiguration.class)
@ConditionalOnBean(CacheManager.class)
@ConditionalOnClass(CacheStatisticsProvider.class)
public class RedissonCacheStatisticsAutoConfiguration 
{
    @Bean
    public RedissonCacheStatisticsProvider redissonCacheStatisticsProvider(){
        return new RedissonCacheStatisticsProvider();
    }
}
```

### `Condition` 注解

通常，所有自动配置类都具有一个或多个`@Conditional`注解。它仅在条件满足时才允许注册`bean`。以下是一些有用的条件注解。

#### `@ConditionalOnBean` 和 `@ConditionalOnMissingBean`

这些注解可根据是否存在特定`bean`来注册某个`bean`。

它的`value`属性用于根据类型或名称指定`bean`。此外，`search`属性允许我们限制在搜索`bean`时应该考虑的`ApplicationContext`层次结构。

如果条件不匹配，则在类级别使用这些注解可防止将`@Configuration`类注册为`Bean`。

在下面的例子中，`JpaTransactionManager` `bean`只有在应用程序上下文中还没有定义类型为`JpaTransactionManager`的`bean`时才会被加载。

```java
@Bean
@ConditionalOnMissingBean(type = "JpaTransactionManager")
JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) 
{
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
}
```

#### `@ConditionalOnClass` 和 `@ConditionalOnMissingClass`

这些注解允许根据特定类的存在与否来包含配置类。注意，注解是使用`spring` `ASM`模块解析的，即使在运行时没有类，也可以在注解中引用该类。

我们还可以使用`value`属性来引用实际的类，或者使用`name`属性来通过使用字符串值来指定类名。

下面的配置将创建`EmbeddedAcmeService`，前提是这个类在运行时可用，并且在应用程序上下文中不存在其他具有相同名称的`bean`。

```java
@Configuration
@ConditionalOnClass(EmbeddedAcmeService.class)
static class EmbeddedConfiguration 
{
 
    @Bean
    @ConditionalOnMissingBean
    public EmbeddedAcmeService embeddedAcmeService() { 
 }
 
}
```

#### `@ConditionalOnNotWebApplication` 和 `@ConditionalOnWebApplication`

这些注解允许根据应用程序是否是`web`应用程序来注册`bean`。在`Spring`中，`web`应用程序至少满足以下三个需求之一:

1. 使用`Spring` `WebApplicationContext`
2. 定义`session`的 `scope`
3. 存在`StandardServletEnvironment`

#### `@ConditionalOnProperty`

该注释允许基于`Spring`环境属性的存在和值进行配置。

例如，如果我们针对不同的环境具有不同的数据源定义，则可以使用此注解。

```java
@Bean
@ConditionalOnProperty(name = "env", havingValue = "local")
DataSource dataSource() 
{
    // ...
}
 
@Bean
@ConditionalOnProperty(name = "env", havingValue = "prod")
DataSource dataSource() 
{
    // ...
}
```

#### `@ConditionalOnResource`

该注释只允许在类路径中出现特定资源时才注解`bean`。可以使用通常的`Spring`约定来指定资源。

```java
@ConditionalOnResource(resources = "classpath:vendor.properties")
Properties additionalProperties() 
{
    // ...
}

```

#### `@ConditionalOnExpression`

该注解允许基于`SpEL`表达式的结果进行配置。当要评估的条件是复杂的且应作为一个条件进行评估时，使用该注解。

```java
@Bean
@ConditionalOnExpression("${env} && ${havingValue == 'local'}")
DataSource dataSource() 
{
    // ...
}

```

#### ` @ConditionalOnCloudPlatform`

该注解允许在指定的云平台处于活动状态时注册`bean`。

```java

@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
public class CloudConfigurationExample 
{
  @Bean
  public MyBean myBean(MyProperties properties) 
  {
    return new MyBean(properties.getParam);
  }
}

```

原文链接：[Spring-boot-starter Maven Templates](https://howtodoinjava.com/spring-boot2/spring-boot-starter-templates/)
