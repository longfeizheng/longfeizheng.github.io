---
layout: post
title: Spring Boot 2.0 整合 Spring Security Oauth2
categories: Security
description: Security
keywords: Security
---

> 是金子在哪都会发光的——每个说这句话的人都误以为自己是金子。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring-security-OAuth205.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth205.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth205.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth205.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth205.png")

## 前言 ##

在[Spring Security源码分析十一：Spring Security OAuth2整合JWT](https://longfeizheng.github.io/2018/01/23/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%8D%81%E4%B8%80-Spring-Security-OAuth2%E6%95%B4%E5%90%88JWT/)中，我们使用`Spring Boot 1.5.6.RELEASE`版本整合`Spring Security Oauth2`实现了授权码模式、密码模式以及用户自定义登录返回`token`。但更新至`Spring Boot 2.0.1.RELEASE`版本时会出现一些小问题。在此，帮大家踩一下坑。关于`OAuth2`请参考[理解OAuth 2.0](http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)

### 修改pom.xml

更新`Spring Boot `版本为`Spring Boot 2.0.1.RELEASE`

```xml
   <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.1.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
```

### 新增SecurityConfig配置

新增`SecurityConfig`用于暴露`AuthenticationManager`

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        AuthenticationManager manager = super.authenticationManagerBean();
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
//                .formLogin().and()
                .httpBasic().and()
                .csrf().disable();
    }
}
```

### 修改MerryyouAuthorizationServerConfig

修改`MerryyouAuthorizationServerConfig`用于加密`clientsecret`和设置重定向地址

```java
......
 @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        InMemoryClientDetailsServiceBuilder build = clients.inMemory();
        if (ArrayUtils.isNotEmpty(oAuth2Properties.getClients())) {
            for (OAuth2ClientProperties config : oAuth2Properties.getClients()) {
                build.withClient(config.getClientId())
                        .secret(passwordEncoder.encode(config.getClientSecret()))
                        .accessTokenValiditySeconds(config.getAccessTokenValiditySeconds())
                        .refreshTokenValiditySeconds(60 * 60 * 24 * 15)
                        .authorizedGrantTypes("refresh_token", "password", "authorization_code")//OAuth2支持的验证模式
                        .redirectUris("http://www.merryyou.cn")
                        .scopes("all");
            }
        }
......
```

### 修改application.yml

由于在2.x版本中由于引入了不同的客户端，需要指定配置哪种连接池。

```yaml
server:
  port: 8888
  redis:
    host: localhost
    port: 6379
    jedis:
      pool:
        max-active: 8
        max-wait: -1
        min-idle: 0
        max-idle: 8
logging:
  level:
    org.springframework: info
merryyou:
  security:
    oauth2:
      storeType: redis #或者jwt
      jwtSigningKey: merryyou
      clients[0]:
        clientId: merryyou
        clientSecret: merryyou
      clients[1]:
              clientId: merryyou1
              clientSecret: merryyou1

```

### 效果如下

#### 授权码模式

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth202.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth202.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth202.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth202.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth202.gif")

#### 密码模式

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth203.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth203.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth203.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth203.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth203.gif")

#### 自定义登录

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth204.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth204.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth204.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth204.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth204.gif")

#### 刷新token

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth205.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth205.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth205.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth205.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth205.gif")

## 代码下载 ##

- github:[springboot2.0-oauth2](https://github.com/longfeizheng/springboot2.0-oauth2)
- gitee:[springboot2.0-oauth2](https://gitee.com/merryyou/springboot2.0-oauth2)

## 参考 ##
- [https://github.com/lexburner/oauth2-demo](https://github.com/lexburner/oauth2-demo)
- [https://stackoverflow.com/questions/49122867/spring-boot-2-0-0-oauth2](https://stackoverflow.com/questions/49122867/spring-boot-2-0-0-oauth2)
- [https://www.jianshu.com/p/be2c09cd27d8?utm_campaign=hugo&utm_medium=reader_share&utm_content=note&utm_source=weixin-friends](https://www.jianshu.com/p/be2c09cd27d8?utm_campaign=hugo&utm_medium=reader_share&utm_content=note&utm_source=weixin-friends)

## 推荐文章
1. [Java创建区块链系列](https://longfeizheng.github.io/categories/#%E5%8C%BA%E5%9D%97%E9%93%BE)
2. [Spring Security源码分析系列](https://longfeizheng.github.io/categories/#Security)
3. [Spring Data Jpa 系列](https://longfeizheng.github.io/categories/#JPA)
4. [【译】数据结构中关于树的一切（java版）](https://longfeizheng.github.io/2018/04/16/%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84%E4%B8%AD%E4%BD%A0%E9%9C%80%E8%A6%81%E7%9F%A5%E9%81%93%E7%9A%84%E5%85%B3%E4%BA%8E%E6%A0%91%E7%9A%84%E4%B8%80%E5%88%87/)
5. [SpringBoot+Docker+Git+Jenkins实现简易的持续集成和持续部署](https://longfeizheng.github.io/2018/04/22/SpringBoot+Docker+Git+Jenkins%E5%AE%9E%E7%8E%B0%E7%AE%80%E6%98%93%E7%9A%84%E6%8C%81%E7%BB%AD%E9%9B%86%E6%88%90%E5%92%8C%E9%83%A8%E7%BD%B2/)

---

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？