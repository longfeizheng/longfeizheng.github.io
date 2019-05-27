---
layout: post
title: Spring MVC请求处理流程
categories: Spring
description: Spring
keywords: Spring
---

>生活不止眼前的苟且，还有永远读不懂的诗和到不了的远方。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.jpg")

## 概述 ##
`Spring MVC` 是一个模型 - 视图 - 控制器（MVC）的`Web`框架建立在中央前端控制器。本章我们从源码分析一下 `Spring MVC`的请求流程。`Spring` 官方提供的流程图如下：

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring01.png")

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
