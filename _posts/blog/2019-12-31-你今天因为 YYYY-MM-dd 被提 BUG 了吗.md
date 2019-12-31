---
layout: post
title: 你今天因为 YYYY-MM-dd 被提 BUG 了吗？
categories: java
description: java
keywords: java
---

>兽炉沈水烟，翠沼残花片，一行行写入相思传。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java36.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java36.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java36.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java36.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java36.jpg")

## BUG 表现##

![https://user-gold-cdn.xitu.io/2019/12/31/16f59fe071ac6020?w=828&h=1792&f=jpeg&s=121630](https://user-gold-cdn.xitu.io/2019/12/31/16f59fe071ac6020?w=828&h=1792&f=jpeg&s=121630 )

## BUG 原因##

`YYYY` 是 `week-based-year`，今天就已经 `2020` 年了
`yyyy` 还是 `2019` 年
> `YYYY` 是表示：当天所在的周属于的年份，一周从周日开始，周六结束，只要本周跨年，那么这周就算入下一年。


原文链接：[https://v2ex.com/t/633650#r_8407403](https://v2ex.com/t/633650#r_8407403)




