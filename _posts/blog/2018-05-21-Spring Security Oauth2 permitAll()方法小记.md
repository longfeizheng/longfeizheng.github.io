---
layout: post
title: Spring Security Oauth2 permitAll()方法小记
categories: Security
description: Security
keywords: Security
---

> 黄鼠狼在养鸡场山崖边立了块碑，写道：“不勇敢地飞下去，你怎么知道自己原来是一只搏击长空的鹰？！” 

>从此以后

>黄鼠狼每天都能在崖底吃到那些摔死的鸡！

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring-security-OAuth207.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth207.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth207.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth207.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth207.png")

## 前言 ##

上周五有网友问道，在使用`spring-security-oauth2`时，虽然配置了`.antMatchers("/permitAll").permitAll()`，但如果在`header` 中 携带 `Authorization Bearer xxxx`，`OAuth2AuthenticationProcessingFilter`还是会去校验`Token`的正确性，如果`Token`合法，可以正常访问，否则，请求失败。他的需求是当配置`.permitAll()`时，即使携带`Token`，也可以直接访问。

## 解决思路 ##

根据[Spring Security源码分析一：Spring Security认证过程](https://longfeizheng.github.io/2018/01/02/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B8%80-Spring-Security%E8%AE%A4%E8%AF%81%E8%BF%87%E7%A8%8B/)得知`spring-security`的认证为一系列过滤器链。我们只需定义一个比`OAuth2AuthenticationProcessingFilter`更早的过滤器拦截指定请求，去除`header`中的`Authorization Bearer xxxx`即可。

## 代码修改 ##

### 添加PermitAuthenticationFilter类

添加`PermitAuthenticationFilter`类拦截指定请求，清空`header`中的`Authorization Bearer xxxx`

```java
@Component("permitAuthenticationFilter")
@Slf4j
public class PermitAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("当前访问的地址:{}", request.getRequestURI());
        if ("/permitAll".equals(request.getRequestURI())) {

            request = new HttpServletRequestWrapper(request) {
                private Set<String> headerNameSet;

                @Override
                public Enumeration<String> getHeaderNames() {
                    if (headerNameSet == null) {
                        // first time this method is called, cache the wrapped request's header names:
                        headerNameSet = new HashSet<>();
                        Enumeration<String> wrappedHeaderNames = super.getHeaderNames();
                        while (wrappedHeaderNames.hasMoreElements()) {
                            String headerName = wrappedHeaderNames.nextElement();
                            if (!"Authorization".equalsIgnoreCase(headerName)) {
                                headerNameSet.add(headerName);
                            }
                        }
                    }
                    return Collections.enumeration(headerNameSet);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return Collections.<String>emptyEnumeration();
                    }
                    return super.getHeaders(name);
                }

                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return null;
                    }
                    return super.getHeader(name);
                }
            };

        }
        filterChain.doFilter(request, response);

    }
}

```

### 添加PermitAllSecurityConfig配置

添加`PermitAllSecurityConfig`配置用于配置`PermitAuthenticationFilter`

```java
@Component("permitAllSecurityConfig")
public class PermitAllSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain,HttpSecurity> {

    @Autowired
    private Filter permitAuthenticationFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(permitAuthenticationFilter, OAuth2AuthenticationProcessingFilter.class);
    }
}

```


### 修改MerryyouResourceServerConfig，增加对制定路径的授权

```java
 @Override
    public void configure(HttpSecurity http) throws Exception {

        // @formatter:off
        http.formLogin()
                .successHandler(appLoginInSuccessHandler)//登录成功处理器
                .and()
                .apply(permitAllSecurityConfig)
                .and()
                .authorizeRequests()
                .antMatchers("/user").hasRole("USER")
                .antMatchers("/forbidden").hasRole("ADMIN")
                .antMatchers("/permitAll").permitAll()
                .anyRequest().authenticated().and()
                .csrf().disable();

        // @formatter:ON
    }
```

- 关于各个路径的说明参考：[使用Spring MVC测试Spring Security Oauth2 API](https://longfeizheng.github.io/2018/05/16/%E4%BD%BF%E7%94%A8Spring-MVC%E6%B5%8B%E8%AF%95Spring-Security-Oauth2-API/)

### 修改测试类SecurityOauth2Test

添加`permitAllWithTokenTest`方法

```java
    @Test
    public void permitAllWithTokenTest() throws Exception{
        final String accessToken = obtainAccessToken();
        log.info("access_token={}", accessToken);
        String content = mockMvc.perform(get("/permitAll").header("Authorization", "bearer " + accessToken+"11"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        log.info(content);
    }
```

- `Authorization bearer xxx 11`后面随机跟了两个参数

### 效果如下

#### 不配置permitAllSecurityConfig时

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth207.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth207.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth207.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth207.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth207.gif")

#### 配置permitAllSecurityConfig时

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth208.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth208.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth208.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth208.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-oauth208.gif")

## 代码下载 ##

- github:[https://github.com/longfeizheng/security-oauth2](https://github.com/longfeizheng/security-oauth2)
- gitee:[https://gitee.com/merryyou/security-oauth2](https://gitee.com/merryyou/security-oauth2)

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