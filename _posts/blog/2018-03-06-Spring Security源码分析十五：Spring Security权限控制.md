---
layout: post
title: Spring Security源码分析十五：Spring Security 页面权限控制
categories: Spring Security
description: Spring Security
keywords: Spring Security
---
> Spring Security是一个能够为基于Spring的企业应用系统提供声明式的安全访问控制解决方案的安全框架。它提供了一组可以在Spring应用上下文中配置的Bean，充分利用了Spring IoC，DI（控制反转Inversion of Control ,DI:Dependency Injection 依赖注入）和AOP（面向切面编程）功能，为应用系统提供声明式的安全访问控制功能，减少了为企业系统安全控制编写大量重复代码的工作。


## 前言
在[Spring Security源码分析十三：Spring Security 基于表达式的权限控制](https://longfeizheng.github.io/2018/01/30/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%8D%81%E4%B8%89-Spring-Security%E6%9D%83%E9%99%90%E6%8E%A7%E5%88%B6/)中，我们只是在后台增加了权限控制，并未在页面做任何处理，与之对应的按钮和链接还是会显示在页面上，用户体验较差。本章使用`Spring Security`标签库来包裹需要保护的内容。

### freemarker中使用security标签
#### 增加security标签库依赖
```xml
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>jstl</artifactId>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-taglibs</artifactId>
</dependency>
```
#### 添加ClassPathTldsLoader类加载security.tld
```java
public class ClassPathTldsLoader {
    private static final String SECURITY_TLD = "/META-INF/security.tld";

    final private List<String> classPathTlds;

    public ClassPathTldsLoader(String... classPathTlds) {
        super();
        if(classPathTlds.length == 0){
            this.classPathTlds = Arrays.asList(SECURITY_TLD);
        }else{
            this.classPathTlds = Arrays.asList(classPathTlds);
        }
    }

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @PostConstruct
    public void loadClassPathTlds() {
        freeMarkerConfigurer.getTaglibFactory().setClasspathTlds(classPathTlds);
    }
}
```
#### 定义ClassPathTldsLoader Bean
```java
   @Bean
    @ConditionalOnMissingBean(ClassPathTldsLoader.class)
    public ClassPathTldsLoader classPathTldsLoader(){
        return new ClassPathTldsLoader();
    }
```
#### 页面中使用标签
```html
<!-- 引入标签-->
<#assign  sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>主页</title>
</head>
<body align="center">
<h2>Spring Security Demo</h2>
	<!-- freemarker使用security标签格式如下-->
    <@sec.authorize access="hasRole('ROLE_ADMIN')">
    you can see this
    </@sec.authorize>
    <@sec.authorize access="isAnonymous()">
    you can see isAnonymous
    </@sec.authorize>
</body>
</html>
```
### 常用的Security标签
#### authorize
`authorize`用于判断用户是否具有对应权限，从而控制其限制的内容，包含以下属性。
##### access
`access`属性需要使用表达式来判断权限，当表达式的返回结果为true时表示拥有对应的权限。
```html
<@sec.authorize access="hasRole('ROLE_ADMIN')">
此内容仅对在授予权限列表中拥有“ROLE_ADMIN”权限的用户可见
</@sec.authorize>
--------------------------
<@sec.authorize access="hasPermission(#domain,'read') or hasPermission(#domain,'write')">
只有具有读取或写入权限的用户才能看到此内容，该用户被发现为名为“domain”的请求属性。
</@sec.authorize>
-----------------------------
<@sec.authorize url="/admin">
此内容仅对有权将请求发送到“/ admin”链接的用户可见
</@sec.authorize>
```
#### authentication
`authentication`标签用来代表当前`Authentication`对象，主要用于获取当前`Authentication`的相关信息。包含以下属性。

##### property
 property属性只允许指定Authentication所拥有的属性。
```html
<!--获取当前用户的用户名-->
<@sec:authentication property="principal.username" />
```
##### var属性
  var属性用于指定一个属性名，这样当获取到了authentication的相关信息后会将其以var指定的属性名进行存放，默认是存放在pageConext中。可以通过scope属性进行指定。此外，当指定了var属性后，authentication标签不会将获取到的信息在页面上进行展示，如需展示用户应该通过var指定的属性进行展示，或去掉var属性。
```html
 <@sec.authentication property="principal.username" scope="session" var="username"/>
${username }
```
#### accesscontrollist
该标签只有在与`spring security`的acl模块一起使用时才有效。它会检查指定域对象的必需权限的逗号分隔列表。如果当前用户拥有所有这些权限，则会评估标签正文。如果他们不这样做，它将被跳过。
```html
<@sec.accesscontrollist hasPermission="1,2" domainObject="${someObject}">
如果用户具有给定对象上的值“1”或“2”表示的所有权限，则会显示此信息
</@sec.accesscontrollist>
```

## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/logback](https://github.com/longfeizheng/logback)

---
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？

