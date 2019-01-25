---
layout: post
title: Small Spring系列四：constructor Injection
categories: Spring
description: Spring
keywords: Spring
---

> 纤云弄巧，飞星传恨，银汉迢迢暗度。金风玉露一相逢，便胜却人间无数。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg")

## 概述 ##

上一章我们已经实现了`setter` 注入,具体实现如下

1. 新增`PropertyValue`类来表达`<property>`标签内容
2. 新增`BeanDefinitionValueResolver`来区分`<property>`中的`ref`属性和`value`属性
3. 使用`jdk`的`PropertyEditorSupport`用于类型转换,因为`xml`都是字符串类型字面值
4. 新增`TypeConverter`封装一些列类型转换器

`spring`配置依赖注入有三种方式,`setter`注入、`constructor`注入和注解注入。我们上一章已实现`setter`注入，本章继续实现`constructor`注入。

## TODO

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190122_constructor_v1](https://github.com/longfeizheng/small-spring/tree/20190122_constructor_v1)


## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
