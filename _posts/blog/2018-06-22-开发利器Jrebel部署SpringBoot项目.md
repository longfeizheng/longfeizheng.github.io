---
layout: post
title: 开发利器JRebel部署SpringBoot项目
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---

> 不要以为年纪轻轻就跌倒了人生谷底，未来还有更大的下降空间等着你。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot06.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot06.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot06.png")

#### idea下载和安装JRebel

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot07.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot07.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot07.png")


#### 激活JRebel

- 访问[https://my.jrebel.com/](https://my.jrebel.com/)
- 使用[facebook](https://www.facebook.com/longfei.zheng.589)或[twitter](https://twitter.com/xiayebaibi)登录

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot08.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot08.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot08.png")


[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot09.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot09.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot09.png")

#### 勾选 Build project automatically

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot10.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot10.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot10.png")

#### 快捷键 ctrl+shift+alt+/ 选择 Retistry

- 勾选compiler.automake.allow.when.app.running

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot11.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot11.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot11.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot11.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot11.png")

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot12.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot12.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot12.png")

#### 勾选需要热部署的模块

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot02.gif")

- resources目录下面会多出一个rebel.xml

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot17.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot17.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot17.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot17.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot17.png")

#### 配置 Run Dashboard

- 默认启动控制台

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot13.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot13.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot13.png")

- 配置Run Dashboard控制台

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot14.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot14.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot14.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot14.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot14.png")

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot15.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot15.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot15.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot15.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot15.png")

- Run Dashboard控制台

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot16.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot16.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot16.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot16.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot16.png")

#### 使用JRebel 启动后效果如下

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot03.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot03.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot03.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot03.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot03.gif")

#### 最新激活方式

[https://blog.xihefeng.com/archives/205.html](https://blog.xihefeng.com/archives/205.html)


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
```

```