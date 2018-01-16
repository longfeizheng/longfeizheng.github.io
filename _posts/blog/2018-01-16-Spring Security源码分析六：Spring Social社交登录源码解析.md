---
layout: post
title: Spring Security源码分析六：Spring Social社交登录源码解析
categories: Spring Security
description: Spring Security
keywords: Spring Security
---
> 在[Spring Security源码分析三：Spring Social实现QQ社交登录](https://longfeizheng.github.io/2018/01/09/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B8%89-Spring-Social%E7%A4%BE%E4%BA%A4%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/)和[Spring Security源码分析四：Spring Social实现微信社交登录](https://longfeizheng.github.io/2018/01/12/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%9B%9B-Spring-Social%E7%A4%BE%E4%BA%A4%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/)这两章中，我们使用`Spring Social`已经实现了国内最常用的`QQ`和`微信`社交登录。本章我们来简单分析一下`Spring Social`在社交登录的过程中做了哪些事情？（`微博`社交登录也已经实现，由于已经连续两篇介绍社交登录，所以不在单开一章节描述）

## 引言 ##

OAuth2是一种授权协议，简单理解就是它可以让用户在不将用户名密码交给第三方应用的情况下，第三方应用有权访问用户存在服务提供商上面的数据。


### Spring Social 基本原理
[![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Social-Sequence.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Social-Sequence.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Social-Sequence.png")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Social-Sequence.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Social-Sequence.png")

1. 访问第三方应用
2. 将用户请求导向服务提供商
3. 用户同意授权
4. 携带授权码返回第三方莹莹
5. 第三方应用携带授权码到服务提供商申请令牌
6. 服务提供商返回令牌
7. 获取用户基本信息
8. 根据用户信息构建`Authentication`放入`SecurityContext中`
如果在`SecurityContext`中放入一个已经认证过的`Authentication`实例，那么对于`Spring Security`来说，已经成功登录


`Spring Social`就是为我们将`OAuth2`认证流程封装到`SocialAuthenticationFilter`过滤器中，并根据返回的用户信息构建`Authentication`。前面我们已经提到`Spring Security`的[验证逻辑](https://longfeizheng.github.io/2018/01/02/Spring-Security%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B8%80-Spring-Security%E8%AE%A4%E8%AF%81%E8%BF%87%E7%A8%8B/#%E9%AA%8C%E8%AF%81%E9%80%BB%E8%BE%91)从而实现使用社交登录。

启动[logback](https://github.com/longfeizheng/logback)断点调试；

[![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Security-chain.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Security-chain.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Security-chain.png")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Security-chain.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/Spring-Security-chain.png")

1. `ValidateCodeFilter`校验验证码过滤器
2. `SocialAuthenticationFilter`社交登录过滤器
3. `UsernamePasswordAuthenticationFilter`用户名密码登录过滤器
4. `SmsCodeAuthenticationFilter`短信登录过滤器
5. `AnonymousAuthenticationFilter`前面过滤器都没校验时匿名验证的过滤器
6. `ExceptionTranslationFilter`处理`FilterSecurityInterceptor`授权失败时的过滤器
7. `FilterSecurityInterceptor`授权过滤器

本章我们主要讲解`SocialAuthenticationFilter`

#### SocialAuthenticationFilter

```java
public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		//#1.判断用户是否允许授权
		if (detectRejection(request)) {
			if (logger.isDebugEnabled()) {
				logger.debug("A rejection was detected. Failing authentication.");
			}
			throw new SocialAuthenticationException("Authentication failed because user rejected authorization.");
		}
		
		Authentication auth = null;
		//#2.获取所有的社交配置providerId(本项目中三个：qq,weixin,weibo)
		Set<String> authProviders = authServiceLocator.registeredAuthenticationProviderIds();
		//#3.根据请求获取当前的是那种类型的社交登录
		String authProviderId = getRequestedProviderId(request);
		//#4.判断是否系统中是否配置当前社交providerId
		if (!authProviders.isEmpty() && authProviderId != null && authProviders.contains(authProviderId)) {
			//#5.获取当前社交的处理类即OAuth2AuthenticationService用于获取Authentication
			SocialAuthenticationService<?> authService = authServiceLocator.getAuthenticationService(authProviderId);
			//#6.获取SocialAuthenticationToken
			auth = attemptAuthService(authService, request, response);
			if (auth == null) {
				throw new AuthenticationServiceException("authentication failed");
			}
		}
		return auth;
	}
	
	private Authentication attemptAuthService(final SocialAuthenticationService<?> authService, final HttpServletRequest request, HttpServletResponse response) 
			throws SocialAuthenticationRedirectException, AuthenticationException {
		//获取SocialAuthenticationToken
		final SocialAuthenticationToken token = authService.getAuthToken(request, response);
		if (token == null) return null;
		
		Assert.notNull(token.getConnection());
		//#7.从SecurityContext获取Authentication判断是否授权
		Authentication auth = getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return doAuthentication(authService, request, token);
		} else {
			addConnection(authService, request, token, auth);
			return null;
		}		
	}
	
```

1. 判断用户是否允许授权
2. 获取系统的允许的社交登录配置信息
3. 获取当前的社交登录信息
4. 判断当前的信息是否存在系统配置中
5. 获取处理社交的`OAuth2AuthenticationService`（用于获取`SocialAuthenticationToken`）
6. 从`SecurityContext`获取`Authentication`判断是否授权

#### OAuth2AuthenticationService

```java
public SocialAuthenticationToken getAuthToken(HttpServletRequest request, HttpServletResponse response) throws SocialAuthenticationRedirectException {
		//#1. 获取code
		String code = request.getParameter("code");
		//#2. 判断code值
		if (!StringUtils.hasText(code)) {
			//#3.如果code不存在则抛出SocialAuthenticationRedirectException
			OAuth2Parameters params =  new OAuth2Parameters();
			params.setRedirectUri(buildReturnToUrl(request));
			setScope(request, params);
			params.add("state", generateState(connectionFactory, request));
			addCustomParameters(params);
			throw new SocialAuthenticationRedirectException(getConnectionFactory().getOAuthOperations().buildAuthenticateUrl(params));
		} else if (StringUtils.hasText(code)) {
			try {
				//#4.如果code存在则根据code获得access_token
				String returnToUrl = buildReturnToUrl(request);
				AccessGrant accessGrant = getConnectionFactory().getOAuthOperations().exchangeForAccess(code, returnToUrl, null);
				// TODO avoid API call if possible (auth using token would be fine)
				//#5.用access_token获取用户的信息并返回spring Social标准信息模型
				Connection<S> connection = getConnectionFactory().createConnection(accessGrant);
				//#6.使用返回的用户信息构建SocialAuthenticationToken
				return new SocialAuthenticationToken(connection, null);
			} catch (RestClientException e) {
				logger.debug("failed to exchange for access", e);
				return null;
			}
		} else {
			return null;
		}
	}
```

1. 获取`code` 
2. 判断当前`code`是否存在值
3. 如果不存在则将用户导向授权的地址
4. 如果存在则根据`code`获取`access_token`
5. 根据`access_token`返回用户信息（该信息为`Spring Social`标准信息模型）
6. 使用用户返回的信息构建`SocialAuthenticationToken`

