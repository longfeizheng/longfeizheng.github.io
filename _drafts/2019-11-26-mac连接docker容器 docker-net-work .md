---
layout: post
title: Docker for Mac 容器网络的直连 docker-mac-network
categories: Docker
description: Docker
keywords: Docker
---

>滴不尽相思血泪抛红豆，开不完春柳春花满画楼。

a

## 概述 ##

用 `Docker for Mac` 已经很久了，用它跑本地开发环境可以说是非常方便。自从有了它，需要什么环境，直接搜索镜像`run`就可以了。但`Docker for Mac`一直存在一个问题，即主机无法
直接`ping`通容器。通常我们会通过`-p`添加端口映射。但当我使用`docker`搭建`fastdfs`测试时，需要宿主主机与容器进行直连，但在`Mac`上无法实现。

### `docker-mac-network`

该解决方案使你可以使用`OpenVPN`从`macOS`主机直接访问`Mac`的`Docker for Mac`内部网络。

1. 安装`Tunnelblick`
    ```shell script
    brew cask install tunnelblick
    ```
2. 克隆`docker-mac-network`项目
    ```shell script
    git clone https://github.com/wojas/docker-mac-network.git
    ```
3. 修改`run.sh`文件里的`ip`和子网掩码，于容器中的保持一致
    ```shell script
    vim helpers/run.sh
    ```
    a
    b
4. 切换到`docker-mac-network`目录下执行 `docker-compose up` 执行时间略长会在同级目录下看到`docker-for-mac.ovpn`文件
    c

5. 双击`docker-for-mac.ovpn`文件，提示使用`tunnelblick`打开，点击链接即可
    d
6. 测试
   f

### 重新生成`docker-for-mac.ovpn`文件

```shell script
rm -rf config/*
rm -rf docker-for-mac.ovpn
```

参考链接：
- [mac连接docker容器 docker-mac-network](https://blog.csdn.net/z457181562/article/details/96144248)
- [docker-mac-network](https://github.com/wojas/docker-mac-network)