---
layout: post
title: Spring Boot – 自动配置
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---

>他生莫作有情痴，人间无地着相思。

a

## 概述 ##
`spring boot` 是很容易上手的，它在内部做了很多你不知道的事情。将来，一个好的开发人员将确切地知道`SpringBoot`自动配置背后发生了什么，如何对你有利地使用它，以及如何禁用某些你不想在项目中使用的部分。

为了了解`Spring Boot`背后的大多数基本内容，我们将创建一个具有单个依赖项和单个启动类文件的最小启动应用程序。然后，我们将分析启动日志以获取见解。

###使用启动类创建`Spring Boot`应用程序

1. 在`Eclipse`中创建一个新的`Maven`项目，原型为`maven-archetype-quickstart`。
2. 使用`spring-boot-starter-web`依赖项和插件信息更新`pom.xml`文件
   ```xml
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd;
        <modelVersion>4.0.0</modelVersion>
     
        <groupId>com.howtodoinjava</groupId>
        <artifactId>springbootdemo</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <packaging>jar</packaging>
     
        <name>springbootdemo</name>
        <url>http://maven.apache.org</url>
     
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.0.0.RELEASE</version>
        </parent>
     
        <properties>
            <java.version>1.8</java.version>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        </properties>
     
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>
        </dependencies>
     
        <build>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                </plugin>
            </plugins>
        </build>
     
        <repositories>
            <repository>
                <id>repository.spring.release</id>
                <name>Spring GA Repository</name>
                <url>http://repo.spring.io/release</url>
            </repository>
        </repositories>
    </project>
    ```
3. 创建启动类
    ```java
    package cn.howtodoinjava.demo;
     
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.context.ApplicationContext;
     
    @SpringBootApplication
    public class App 
    {
        public static void main(String[] args) 
        {
            ApplicationContext ctx = SpringApplication.run(App.class, args);
        }
    }
    ```
   #### 启动类做了些什么？
   
   上面的类称为`Spring Boot`应用程序启动类。它通过`Java` `main（）`方法引导和启动`Spring`应用程序。它通常会执行以下操作–
   - 创建`Spring`的`ApplicationContext`的实例。
   - 允许该功能接受命令行参数并将它们作为`Spring`属性公开。
   - 根据配置加载所有`Spring bean`，还可以根据项目需要进行其他操作。
   
### `@SpringBootApplication`注解

此注解是在一条语句中应用3个注解的快捷语句–

1. #### `@SpringBootConfiguration`
    `@SpringBootConfiguration`是`Spring boot 2`中的新注解。之前，我们一直在使用`@Configuration`注解。你也可以使用`@Configuration`代替它。两者都是同一回事。
    
    它表明类提供了`Spring`启动应用程序`@Configuration`。它只是意味着带注解的类是一个配置类，应该扫描它以获得进一步的配置和`bean`定义。

2. #### `@EnableAutoConfiguration`
    该注解用于启用`Spring Application` `Context`的自动配置，尝试猜测和配置你可能需要的`bean`。通常根据你的类路径和定义的`bean`来应用自动配置类。
    
    自动配置会尝试尽可能智能化，并且在你定义更多自己的配置时会自动退出。你始终可以使用两种方法手动排除你不想应用的任何配置
    
    -. 使用 `excludeName()`
    -. 配置文件中使用`spring.autoconfigure.exclude property` 
    
    ```java
    @EnableAutoConfiguration(excludeName = {"multipartResolver","mbeanServer"})
    ```
   
   自动配置始终在注册用户定义的`bean`之后应用。
   
3. #### `@ComponentScan`
    该注解支持与`Spring XML` `context:component-scan `并行
    
    可以指定`basePackageClasses（）`或`basePackages（）`来定义要扫描的特定程序包。如果未定义特定的程序包，则将从声明此注解的类的程序包中进行扫描
    
### 运行启动应用程序并检查日志

在`IDE`中，右键单击应用程序类，然后将其作为`Java`应用程序运行。为了了解已注册的`bean`，我添加了如下修改的启动应用程序。

```java
@SpringBootApplication
public class App 
{
    public static void main(String[] args) 
    {
        ApplicationContext ctx = SpringApplication.run(App.class, args);
         
        String[] beanNames = ctx.getBeanDefinitionNames();
         
        Arrays.sort(beanNames);
 
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }
}
```

日志输入如下

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.0.RELEASE)
 
2018-04-02 13:09:41.100  INFO 11452 --- [           main] com.howtodoinjava.demo.App               : Starting App on FFC15B4E9C5AA with PID 11452 (C:\Users\zkpkhua\IDPPaymentTransfers_Integrated\springbootdemo\target\classes started by zkpkhua in C:\Users\zkpkhua\IDPPaymentTransfers_Integrated\springbootdemo)
2018-04-02 13:09:41.108  INFO 11452 --- [           main] com.howtodoinjava.demo.App               : No active profile set, falling back to default profiles: default
2018-04-02 13:09:41.222  INFO 11452 --- [           main] ConfigServletWebServerApplicationContext : Refreshing org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@4450d156: startup date [Mon Apr 02 13:09:41 IST 2018]; root of context hierarchy
2018-04-02 13:09:43.474  INFO 11452 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2018-04-02 13:09:43.526  INFO 11452 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2018-04-02 13:09:43.526  INFO 11452 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet Engine: Apache Tomcat/8.5.28
2018-04-02 13:09:43.748  INFO 11452 --- [ost-startStop-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2018-04-02 13:09:43.748  INFO 11452 --- [ost-startStop-1] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 2531 ms
2018-04-02 13:09:43.964  INFO 11452 --- [ost-startStop-1] o.s.b.w.servlet.ServletRegistrationBean  : Servlet dispatcherServlet mapped to [/]
2018-04-02 13:09:43.969  INFO 11452 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'characterEncodingFilter' to: [/*]
2018-04-02 13:09:43.970  INFO 11452 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
2018-04-02 13:09:43.970  INFO 11452 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'httpPutFormContentFilter' to: [/*]
2018-04-02 13:09:43.970  INFO 11452 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'requestContextFilter' to: [/*]
2018-04-02 13:09:44.480  INFO 11452 --- [           main] s.w.s.m.m.a.RequestMappingHandlerAdapter : Looking for @ControllerAdvice: org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@4450d156: startup date [Mon Apr 02 13:09:41 IST 2018]; root of context hierarchy
2018-04-02 13:09:44.627  INFO 11452 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
2018-04-02 13:09:44.630  INFO 11452 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error],produces=}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
2018-04-02 13:09:44.681  INFO 11452 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2018-04-02 13:09:44.682  INFO 11452 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2018-04-02 13:09:44.747  INFO 11452 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
2018-04-02 13:09:45.002  INFO 11452 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2018-04-02 13:09:45.070  INFO 11452 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2018-04-02 13:09:45.076  INFO 11452 --- [           main] com.howtodoinjava.demo.App               : Started App in 4.609 seconds (JVM running for 5.263)
app
basicErrorController
beanNameHandlerMapping
beanNameViewResolver
characterEncodingFilter
conventionErrorViewResolver
defaultServletHandlerMapping
defaultValidator
defaultViewResolver
dispatcherServlet
dispatcherServletRegistration
error
errorAttributes
errorPageCustomizer
errorPageRegistrarBeanPostProcessor
faviconHandlerMapping
faviconRequestHandler
handlerExceptionResolver
hiddenHttpMethodFilter
httpPutFormContentFilter
httpRequestHandlerAdapter
jacksonCodecCustomizer
jacksonObjectMapper
jacksonObjectMapperBuilder
jsonComponentModule
localeCharsetMappingsCustomizer
mappingJackson2HttpMessageConverter
mbeanExporter
mbeanServer
messageConverters
methodValidationPostProcessor
multipartConfigElement
multipartResolver
mvcContentNegotiationManager
mvcConversionService
mvcHandlerMappingIntrospector
mvcPathMatcher
mvcResourceUrlProvider
mvcUriComponentsContributor
mvcUrlPathHelper
mvcValidator
mvcViewResolver
objectNamingStrategy
org.springframework.boot.autoconfigure.AutoConfigurationPackages
org.springframework.boot.autoconfigure.condition.BeanTypeRegistry
org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration
org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration
org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration$StringHttpMessageConverterConfiguration
org.springframework.boot.autoconfigure.http.JacksonHttpMessageConvertersConfiguration
org.springframework.boot.autoconfigure.http.JacksonHttpMessageConvertersConfiguration$MappingJackson2HttpMessageConverterConfiguration
org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration$JacksonCodecConfiguration
org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration
org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory
org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$Jackson2ObjectMapperBuilderCustomizerConfiguration
org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$JacksonObjectMapperBuilderConfiguration
org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$JacksonObjectMapperConfiguration
org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$ParameterNamesModuleConfiguration
org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration
org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration
org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration$TomcatWebServerFactoryCustomizerConfiguration
org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletConfiguration
org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration
org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryConfiguration$EmbeddedTomcat
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$EnableWebMvcConfiguration
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter$FaviconConfiguration
org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$DefaultErrorViewResolverConfiguration
org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$WhitelabelErrorViewConfiguration
org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration
org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration$TomcatWebSocketConfiguration
org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata
org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
org.springframework.context.annotation.internalAutowiredAnnotationProcessor
org.springframework.context.annotation.internalCommonAnnotationProcessor
org.springframework.context.annotation.internalConfigurationAnnotationProcessor
org.springframework.context.annotation.internalRequiredAnnotationProcessor
org.springframework.context.event.internalEventListenerFactory
org.springframework.context.event.internalEventListenerProcessor
parameterNamesModule
preserveErrorControllerTargetClassPostProcessor
propertySourcesPlaceholderConfigurer
requestContextFilter
requestMappingHandlerAdapter
requestMappingHandlerMapping
resourceHandlerMapping
restTemplateBuilder
server-org.springframework.boot.autoconfigure.web.ServerProperties
servletWebServerFactoryCustomizer
simpleControllerHandlerAdapter
spring.http.encoding-org.springframework.boot.autoconfigure.http.HttpEncodingProperties
spring.info-org.springframework.boot.autoconfigure.info.ProjectInfoProperties
spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties
spring.mvc-org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties
spring.resources-org.springframework.boot.autoconfigure.web.ResourceProperties
spring.security-org.springframework.boot.autoconfigure.security.SecurityProperties
spring.servlet.multipart-org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
standardJacksonObjectMapperBuilderCustomizer
stringHttpMessageConverter
tomcatServletWebServerFactory
tomcatServletWebServerFactoryCustomizer
tomcatWebServerFactoryCustomizer
viewControllerHandlerMapping
viewResolver
webServerFactoryCustomizerBeanPostProcessor
websocketContainerCustomizer
welcomePageHandlerMapping
```

你会看到有多少个`bean`自动注册。如果你想更深入地研究为什么要注册任何特定的`bean`？通过在应用程序启动时加入`debug`标志，你可以看到这一点。

只需将`-Ddebug = true`作为`VM`参数传递。

现在，当你运行该应用程序时，您将获得许多`debug`日志，它们具有类似的信息：

```text
CodecsAutoConfiguration.JacksonCodecConfiguration matched:
  - @ConditionalOnClass found required class 'com.fasterxml.jackson.databind.ObjectMapper'; @ConditionalOnMissingClass did not find unwanted class (OnClassCondition)
 
CodecsAutoConfiguration.JacksonCodecConfiguration#jacksonCodecCustomizer matched:
  - @ConditionalOnBean (types: com.fasterxml.jackson.databind.ObjectMapper; SearchStrategy: all) found bean 'jacksonObjectMapper' (OnBeanCondition)
 
DispatcherServletAutoConfiguration.DispatcherServletConfiguration matched:
  - @ConditionalOnClass found required class 'javax.servlet.ServletRegistration'; @ConditionalOnMissingClass did not find unwanted class (OnClassCondition)
  - Default DispatcherServlet did not find dispatcher servlet beans (DispatcherServletAutoConfiguration.DefaultDispatcherServletCondition)
 
DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration matched:
  - @ConditionalOnClass found required class 'javax.servlet.ServletRegistration'; @ConditionalOnMissingClass did not find unwanted class (OnClassCondition)
  - DispatcherServlet Registration did not find servlet registration bean (DispatcherServletAutoConfiguration.DispatcherServletRegistrationCondition)
 
  ...
  ...
  ...
```

上面的日志说明了为什么将特定的`bean`注册到`spring`上下文中。当你调试自动配置问题时，此信息非常有用。

同样，每次我们向`Spring Boot`项目添加新的依赖项时，`Spring Boot`自动配置都会自动尝试基于依赖项配置`Bean`。




原文链接：[Spring Boot2 @SpringBootApplication Auto Configuration](https://howtodoinjava.com/spring-boot2/springbootapplication-auto-configuration/)
