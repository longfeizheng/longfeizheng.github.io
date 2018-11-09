---
layout: page
title: About
description: 谦和之中见卓越
keywords: longfeizheng
comments: true
menu: 关于
permalink: /about/
---


### 0x1 序

> **谦和之中见卓越**


### 0x2 工作状况

> **Java Web Developer 1枚，目前就职资和信电子支付；主要负责后端开发**

### 0x3 职业技能

> **SSH/SSM (Struts2 or Spring MVC)、SpringBoot、Quartz、Shiro**
>
> **Oracle、mysql、PostgresSQL 、Redis**
>
> **HTML、js、jQuery**
>
> **Others: Linux、Docker、Kubernetes**

### 0x4 学习计划

> **Java底层 学习中**
>  
> **Java数据结构 学习中**
> 
> **Java设计模式 学习中**
>
> **Spring Security 学习中**
>
> **Spring 学习中**

### 0x5 See You Again

See You Again
## 联系

{% for website in site.data.social %}
* {{ website.sitename }}：[@{{ website.name }}]({{ website.url }})
{% endfor %}




{% for category in site.data.skills %}
### {{ category.name }}
<div class="btn-inline">
{% for keyword in category.keywords %}
<button class="btn btn-outline" type="button">{{ keyword }}</button>
{% endfor %}
</div>
{% endfor %}


---

<audio  autoplay="autoplay">
  <source src="http://merryyou.cn/lover/music/music_see_you_again.mp3" type="audio/mpeg" />
Your browser does not support the audio element.
</audio>
