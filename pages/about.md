---
layout: page
title: About
description: 人生如逆旅，我亦是行人
keywords: longfeizheng
comments: true
menu: 关于
permalink: /about/
---


### 0x1 序

> **人生如逆旅，我亦是行人**


### 0x2 工作状况

> **Java Web Developer 1枚，目前就职京东数科**

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
>
> **Spring Cloud 学习中**

### 0x5 姑娘

姑娘
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
  <source src="http://merryyou.cn/lover/music/gu_niang.mp3" type="audio/mpeg" />
Your browser does not support the audio element.
</audio>

<iframe type="text/html" width="100%" height="385" src="http://www.youtube.com/embed/gfmjMWjn-Xg" frameborder="0"></iframe>
