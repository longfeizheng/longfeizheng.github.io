---
layout: post
title: SpringBoot+Docker+Git+Jenkins实现简易的持续集成和持续部署
categories: 微服务
description: 微服务
keywords: 微服务
---

> 努力了这么久，但凡有点儿天赋，也该有些成功的迹象了。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.png")

## 前言 ##

本篇文章引导你使用`Jenkins`部署`SpringBoot`项目,同时使用`Docker`和`Git`实现简单的[持续集成](https://baike.baidu.com/item/%E6%8C%81%E7%BB%AD%E9%9B%86%E6%88%90)和持续部署。（项目地址：[sso-merryyou](https://github.com/longfeizheng/sso-merryyou)）

流程图如下：

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.png")

1. `push`代码到`Github`触发`WebHook`。（因网络原因，本篇使用[gitee](https://gitee.com/merryyou/sso-merryyou)）
2. `Jenkins`从仓库拉去代码
3. `mavem`构建项目
4. 代码静态分析
5. 单元测试
6. `build`镜像
7. `push`镜像到镜像仓库（本篇使用的镜像仓库为网易镜像仓库）
8. 更新服务


### Jenkins安装

#### 下载jenkins
从[https://jenkins.io/download/](https://jenkins.io/download/)下载对应的`jenkins`

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker07.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker07.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker07.png")

#### 初始化密码
访问本地：[http://localhost:8080](http://localhost:8080)输入密码

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker04.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker04.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker04.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker04.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker04.png")

####选择插件
进入用户自定义插件界面，选择第二个（因为我们本次构建使用的为`Pipelines `）

勾选与`Pipelines `相关的插件

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker05.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker05.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker05.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker05.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker05.png")

等待插件安装完成

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker06.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker06.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker06.png")

#### 配置用户名和密码


[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker08.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker08.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker08.png")

#### 全局配置

系统管理-》全局工具配置 配置Git,JDK和Maven

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker09.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker09.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker09.png")

#### 安全配置
系统管理-》全局安全配置 

- 勾选Allow anonymous read access
- 取消防止跨站点请求伪造

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker10.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker10.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker10.png")

#### 新建任务
新建任务-》流水线

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker11.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker11.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker11.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker11.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker11.png")

#### 构建脚本
勾选触发远程构建 (WebHooks触发地址)，填写简单的`Pipeline script`

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker12.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker12.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker12.png")

```groovy
#!groovy
pipeline{
	agent any

	stages {

		stage('test'){
			steps {
				echo "hello world"
			
			}
		}			
	}
}
```

#### 测试脚本
立即构建

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker13.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker13.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker13.png")

#### 打印
控制台输出

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker14.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker14.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker14.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker14.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker14.png")

### gitee集成WebHooks


#### 添加SSH公匙


[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker15.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker15.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker15.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker15.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker15.png")

#### 配置WebHooks

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker16.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker16.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker16.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker16.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker16.png")

> 使用[natapp](https://natapp.cn/)实现内网穿透

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker17.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker17.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker17.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker17.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker17.png")

### 修改脚本

修改`Pipeline script`

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker18.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker18.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker18.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker18.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker18.png")

```groovy
#!groovy
pipeline{
	agent any
	//定义仓库地址
	environment {
		REPOSITORY="https://gitee.com/merryyou/sso-merryyou.git"
	}

	stages {

		stage('获取代码'){
			steps {
				echo "start fetch code from git:${REPOSITORY}"
				//清空当前目录
				deleteDir()
				//拉去代码	
				git "${REPOSITORY}"
			}
		}

		stage('代码静态检查'){
			steps {
				//伪代码检查
				echo "start code check"
			}
		}		

		stage('编译+单元测试'){
			steps {
				echo "start compile"
				//切换目录
				dir('sso-client1') {
					//重新打包
					bat 'mvn -Dmaven.test.skip=true -U clean install'
				}
			}
		}

		stage('构建镜像'){
			steps {
				echo "start build image"
				dir('sso-client1') {
					//build镜像
					bat 'docker build -t hub.c.163.com/longfeizheng/sso-client1:1.0 .'
					//登录163云仓库
					bat 'docker login -u longfei_zheng@163.com -p password hub.c.163.com'
					//推送镜像到163仓库
					bat 'docker push hub.c.163.com/longfeizheng/sso-client1:1.0'
				}
			}
		}

		stage('启动服务'){
			steps {
				echo "start sso-merryyou"
				//重启服务
				bat 'docker-compose up -d --build'
			}
		}				

	}
}
```

Pipeline的几个基本概念：

- Stage: 阶段，一个Pipeline可以划分为若干个Stage，每个Stage代表一组操作。注意，Stage是一个逻辑分组的概念，可以跨多个Node。
- Node: 节点，一个Node就是一个Jenkins节点，或者是Master，或者是Agent，是执行Step的具体运行期环境。
- Step: 步骤，Step是最基本的操作单元，小到创建一个目录，大到构建一个Docker镜像，由各类Jenkins Plugin提供。

更多Pipeline语法参考：[pipeline 语法详解](http://www.cnblogs.com/fengjian2016/p/8227532.html)

## 测试 ##

`docker-compose up -d ` 启动服务

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker19.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker19.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker19.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker19.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker19.png")

访问[http://sso-taobao:8083/client1](http://sso-taobao:8083/client1)登录

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker20.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker20.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker20.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker20.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker20.png")

修改内容效果如下：

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker03.gif")

更多效果图

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker21.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker21.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker21.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker21.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker21.png")

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker22.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker22.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker22.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker22.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker22.png")

## 代码下载 ##

- github:[https://github.com/longfeizheng/sso-merryyou](https://github.com/longfeizheng/sso-merryyou)
- gitee:[https://gitee.com/merryyou/sso-merryyou](https://gitee.com/merryyou/sso-merryyou)



## 推荐文章
1. [Java创建区块链系列](https://longfeizheng.github.io/categories/#%E5%8C%BA%E5%9D%97%E9%93%BE)
2. [Spring Security源码分析系列](https://longfeizheng.github.io/categories/#Security)
3. [Spring Data Jpa 系列](https://longfeizheng.github.io/categories/#JPA)
4. [【译】数据结构中关于树的一切（java版）](https://longfeizheng.github.io/2018/04/16/%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84%E4%B8%AD%E4%BD%A0%E9%9C%80%E8%A6%81%E7%9F%A5%E9%81%93%E7%9A%84%E5%85%B3%E4%BA%8E%E6%A0%91%E7%9A%84%E4%B8%80%E5%88%87/)

---

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？