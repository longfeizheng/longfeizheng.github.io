---
layout: post
title: Zookeeper系列一：Zookeeper基础命令操作
categories: Zookeeper
description: Zookeeper
keywords: Zookeeper
---

> 有些事不是努力就可以改变的，五十块的人民币设计的再好看，也没有一百块的招人喜欢。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring-security-OAuth209.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth209.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth209.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth209.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/security/spring-security-OAuth209.png")

## 前言 ##

由于公司年底要更换办公地点，所以最近投了一下简历，发现面试官现在很喜欢问`dubbo`、`zookeeper`和高并发等。由于公司没有使用`dubbo`，只知道`dubbo`是一个远程服务调用的分布式框架，`zookeeper`为分布式应用程序协调服务。因此，本周查阅资料整理下`zookeeper`学习笔记。

###  安装zookeeper

安装参考链接[https://blog.csdn.net/qiunian144084/article/details/79192819](https://blog.csdn.net/qiunian144084/article/details/79192819)

### 基础命令操作

#### 启动zk服务
- `./zkServer.sh start`

```shell
[root@localhost bin]# ./zkServer.sh
ZooKeeper JMX enabled by default
Using config: /usr/home/zookeeper-3.4.11/bin/../conf/zoo.cfg
Usage: ./zkServer.sh {start|start-foreground|stop|restart|status|upgrade|print-cmd}
# 提示要以./zkCli.sh start 启动zk
./zkCli.sh start 
```
####  查看zk的运行状态
- `./zkServer.sh status` 由于我已经配置了`zk`的集群，所以此处显示状态为`leader`
```shell
[root@localhost bin]# ./zkServer.sh status
ZooKeeper JMX enabled by default
Using config: /usr/home/zookeeper-3.4.11/bin/../conf/zoo.cfg
Mode: leader
```
#### 客户端链接zk
```shell
[root@localhost bin]# ./zkCli.sh 
......
WatchedEvent state:SyncConnected type:None path:null
[zk: localhost:2181(CONNECTED) 0]
```
#### help 查看客户端帮助命令
- `help`
```shell
[zk: localhost:2181(CONNECTED) 0] help
ZooKeeper -server host:port cmd args
	stat path [watch]
	set path data [version]
	ls path [watch]
	delquota [-n|-b] path
	ls2 path [watch]
	setAcl path acl
	setquota -n|-b val path
	history 
	redo cmdno
	printwatches on|off
	delete path [version]
	sync path
	listquota path
	rmr path
	get path [watch]
	create [-s] [-e] path data acl
	addauth scheme auth
	quit 
	getAcl path
	close 
	connect host:port
[zk: localhost:2181(CONNECTED) 1] 
```
#### ls 查看
- `ls`  查看命令(`niocoder`是我测试集群创建的节点，默认只有`zookeeper`一个节点)
```shell
[zk: localhost:2181(CONNECTED) 1] ls /
[niocoder, zookeeper]
[zk: localhost:2181(CONNECTED) 2] ls /zookeeper 
[quota]
[zk: localhost:2181(CONNECTED) 4] ls /zookeeper/quota
[]
```
####  get 获取节点数据和更新信息
- `get`内容为空
- cZxid ：创建节点的id
- ctime ： 节点的创建时间
- mZxid ：修改节点的id
- mtime ：修改节点的时间
- pZxid ：子节点的id
- cversion : 子节点的版本
- dataVersion ： 当前节点数据的版本
- aclVersion ：权限的版本
- ephemeralOwner ：判断是否是临时节点
- dataLength ： 数据的长度
- numChildren ：子节点的数量
```shell
[zk: localhost:2181(CONNECTED) 7] get /zookeeper #下面空行说明节点内容为空

cZxid = 0x0
ctime = Thu Jan 01 00:00:00 UTC 1970
mZxid = 0x0
mtime = Thu Jan 01 00:00:00 UTC 1970
pZxid = 0x0
cversion = -1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 1
[zk: localhost:2181(CONNECTED) 8]
```
#### stat 获得节点的更新信息
- `stat`
```shell
[zk: localhost:2181(CONNECTED) 8] stat /zookeeper
cZxid = 0x0
ctime = Thu Jan 01 00:00:00 UTC 1970
mZxid = 0x0
mtime = Thu Jan 01 00:00:00 UTC 1970
pZxid = 0x0
cversion = -1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 1
```
#### ls2 ls命令和stat命令的整合
- `ls2`
```shell
[zk: localhost:2181(CONNECTED) 10] ls2 /zookeeper
[quota]
cZxid = 0x0
ctime = Thu Jan 01 00:00:00 UTC 1970
mZxid = 0x0
mtime = Thu Jan 01 00:00:00 UTC 1970
pZxid = 0x0
cversion = -1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 1
[zk: localhost:2181(CONNECTED) 11] 
```
#### create 创建节点
- `create [-s] [-e] path data acl` 可以注意一下各个版本的变化

```shell
#创建merryyou节点，节点的内容为merryyou
[zk: localhost:2181(CONNECTED) 1] create /merryyou merryyou
Created /merryyou
#获得merryyou节点内容
[zk: localhost:2181(CONNECTED) 3] get /merryyou
merryyou
cZxid = 0x200000004
ctime = Sat Jun 02 14:20:06 UTC 2018
mZxid = 0x200000004
mtime = Sat Jun 02 14:20:06 UTC 2018
pZxid = 0x200000004
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 0
```
#### create -e  创建临时节点
- `create -e`
```shell
#创建临时节点
[zk: localhost:2181(CONNECTED) 4] create -e  /merryyou/temp merryyou
Created /merryyou/temp
[zk: localhost:2181(CONNECTED) 5] get /merryyou
merryyou
cZxid = 0x200000004
ctime = Sat Jun 02 14:20:06 UTC 2018
mZxid = 0x200000004
mtime = Sat Jun 02 14:20:06 UTC 2018
pZxid = 0x200000005
cversion = 1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 1
[zk: localhost:2181(CONNECTED) 6] get /merryyou/temp
merryyou
cZxid = 0x200000005
ctime = Sat Jun 02 14:22:24 UTC 2018
mZxid = 0x200000005
mtime = Sat Jun 02 14:22:24 UTC 2018
pZxid = 0x200000005
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x2000000d4500000
dataLength = 8
numChildren = 0
[zk: localhost:2181(CONNECTED) 7] 
#断开重连之后，临时节点自动消失
WATCHER::

WatchedEvent state:SyncConnected type:None path:null
# 因为默认的心跳机制，此时查询临时节点还存在
[zk: localhost:2181(CONNECTED) 0] ls /merryyou
[temp]
#再次查询，临时节点消失
[zk: localhost:2181(CONNECTED) 1] ls /merryyou
[]
[zk: localhost:2181(CONNECTED) 2] 
```
#### create -s  创建顺序节点 自动累加
- `create -s`
```shell
# 创建顺序节点，顺序节点会自动累加
[zk: localhost:2181(CONNECTED) 2] create -s /merryyou/sec seq
Created /merryyou/sec0000000001
[zk: localhost:2181(CONNECTED) 3] create -s /merryyou/sec seq
Created /merryyou/sec0000000002 
```
#### set path data [version] 修改节点
```shell
[zk: localhost:2181(CONNECTED) 6] get /merryyou
merryyou
cZxid = 0x200000004
ctime = Sat Jun 02 14:20:06 UTC 2018
mZxid = 0x200000004
mtime = Sat Jun 02 14:20:06 UTC 2018
pZxid = 0x200000009
cversion = 4
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 2
# 修改节点内容为new-merryyou
[zk: localhost:2181(CONNECTED) 7] set /merryyou new-merryyou
cZxid = 0x200000004
ctime = Sat Jun 02 14:20:06 UTC 2018
mZxid = 0x20000000a
mtime = Sat Jun 02 14:29:23 UTC 2018
pZxid = 0x200000009
cversion = 4
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 12
numChildren = 2
#再次查询，节点内容已经修改
[zk: localhost:2181(CONNECTED) 8] get /merryyou
new-merryyou
cZxid = 0x200000004
ctime = Sat Jun 02 14:20:06 UTC 2018
mZxid = 0x20000000a
mtime = Sat Jun 02 14:29:23 UTC 2018
pZxid = 0x200000009
cversion = 4
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 12
numChildren = 2
# set 根据版本号更新 dataVersion 乐观锁
[zk: localhost:2181(CONNECTED) 9] set /merryyou test-merryyou 1
cZxid = 0x200000004
ctime = Sat Jun 02 14:20:06 UTC 2018
mZxid = 0x20000000b
mtime = Sat Jun 02 14:31:30 UTC 2018
pZxid = 0x200000009
cversion = 4
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 13
numChildren = 2
# 因为数据的版本号已经修改为2 再次使用版本号1修改节点提交错误
[zk: localhost:2181(CONNECTED) 10] set /merryyou test-merryyou 1
version No is not valid : /merryyou
```
#### delete path [version] 删除节点
```shell
[zk: localhost:2181(CONNECTED) 13] delete /merryyou/sec000000000

sec0000000001   sec0000000002
[zk: localhost:2181(CONNECTED) 13] delete /merryyou/sec0000000001
[zk: localhost:2181(CONNECTED) 14] ls /merryyou
[sec0000000002]
[zk: localhost:2181(CONNECTED) 15] 
# 版本号操作与set类似 version
```

#### watcher通知机制

关于`watcher`机制大体的理解可以为，当每个节点发生变化，都会触发`watcher`事件，类似于`mysql`的触发器。`zk`中 `watcher`是一次性的，触发后立即销毁。可以参考[https://blog.csdn.net/hohoo1990/article/details/78617336](https://blog.csdn.net/hohoo1990/article/details/78617336)
- `stat path [watch]` 设置watch事件
- `get path [watch] `设置watch事件
- 子节点创建和删除时触发watch事件，子节点修改不会触发该事件

##### stat path [watch] 设置watch事件
```shell
# 添加watch 事件
[zk: localhost:2181(CONNECTED) 18] stat /longfei watch
Node does not exist: /longfei
# 创建longfei节点时触发watcher事件
[zk: localhost:2181(CONNECTED) 19] create /longfei test

WATCHER::

WatchedEvent state:SyncConnected type:NodeCreated path:/longfei
Created /longfei
```
##### get path [watch] 设置watch事件
```shell
# 使用get命令添加watch事件
[zk: localhost:2181(CONNECTED) 20] get /longfei watch
test
cZxid = 0x20000000e
ctime = Sat Jun 02 14:43:15 UTC 2018
mZxid = 0x20000000e
mtime = Sat Jun 02 14:43:15 UTC 2018
pZxid = 0x20000000e
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 4
numChildren = 0
#修改节点触发watcher事件
[zk: localhost:2181(CONNECTED) 21] set /longfei new_test

WATCHER::

WatchedEvent state:SyncConnected type:NodeDataChanged path:/longfei
cZxid = 0x20000000e
ctime = Sat Jun 02 14:43:15 UTC 2018
mZxid = 0x20000000f
mtime = Sat Jun 02 14:45:06 UTC 2018
pZxid = 0x20000000e
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 0
[zk: localhost:2181(CONNECTED) 22] 
# 删除触发watcher事件
[zk: localhost:2181(CONNECTED) 23] get /longfei watch
new_test
cZxid = 0x20000000e
ctime = Sat Jun 02 14:43:15 UTC 2018
mZxid = 0x20000000f
mtime = Sat Jun 02 14:45:06 UTC 2018
pZxid = 0x20000000e
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 0
[zk: localhost:2181(CONNECTED) 24] delete /longfei

WATCHER::

WatchedEvent state:SyncConnected type:NodeDeleted path:/longfei
[zk: localhost:2181(CONNECTED) 25] 
```
#### ACL权限控制
ZK的节点有5种操作权限：`CREATE`、`READ`、`WRITE`、`DELETE`、`ADMIN` 也就是 增、删、改、查、管理权限，这5种权限简写为`crwda`(即：每个单词的首字符缩写)。
注：这5种权限中，delete是指对子节点的删除权限，其它4种权限指对自身节点的操作权限

身份的认证有4种方式：
- `world`：默认方式，相当于全世界都能访问
- `auth`：代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户)
- `digest`：即用户名:密码这种方式认证，这也是业务系统中最常用的
- `ip`：使用Ip地址认证

使用`[scheme:id:permissions]`来表示acl权限

##### getAcl:获取某个节点的acl权限信息
```shell
#获取节点权限信息默认为 world:cdrwa任何人都可以访问
[zk: localhost:2181(CONNECTED) 34] getAcl /merryyou
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 35] 
```
##### setAcl 设置权限
```shell
[zk: localhost:2181(CONNECTED) 35] create /merryyou/test test
Created /merryyou/test
[zk: localhost:2181(CONNECTED) 36] getAcl /merryyou/test
'world,'anyone
: cdrwa
# 设置节点权限 crwa 不允许删除
[zk: localhost:2181(CONNECTED) 37] setAcl /merryyou/test world:anyone:crwa
cZxid = 0x200000018
ctime = Sat Jun 02 16:18:18 UTC 2018
mZxid = 0x200000018
mtime = Sat Jun 02 16:18:18 UTC 2018
pZxid = 0x200000018
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 4
numChildren = 0
# 查询刚才设置的acl权限信息 crwa 没有删除权限
[zk: localhost:2181(CONNECTED) 38] getAcl /merryyou/test
'world,'anyone
: crwa
[zk: localhost:2181(CONNECTED) 39] 
[zk: localhost:2181(CONNECTED) 39] create /merryyou/test/abc abc
Created /merryyou/test/abc
# 删除子节点的时候提交权限不足
[zk: localhost:2181(CONNECTED) 40] delete /merryyou/test/abc
Authentication is not valid : /merryyou/test/abc
# 设置节点的权限信息为rda
[zk: localhost:2181(CONNECTED) 41] setAcl /merryyou/test world:anyone:rda 
cZxid = 0x200000018
ctime = Sat Jun 02 16:18:18 UTC 2018
mZxid = 0x200000018
mtime = Sat Jun 02 16:18:18 UTC 2018
pZxid = 0x20000001a
cversion = 1
dataVersion = 0
aclVersion = 2
ephemeralOwner = 0x0
dataLength = 4
numChildren = 1
[zk: localhost:2181(CONNECTED) 42] getAcl /merryyou/test
'world,'anyone
: dra
# 可以成功删除
[zk: localhost:2181(CONNECTED) 43] delete /merryyou/test/abc             
[zk: localhost:2181(CONNECTED) 46] ls /merryyou/test
[]
[zk: localhost:2181(CONNECTED) 47] 
# 设置节点信息为a admin
[zk: localhost:2181(CONNECTED) 47] setAcl /merryyou/test world:anyone:a  
cZxid = 0x200000018
ctime = Sat Jun 02 16:18:18 UTC 2018
mZxid = 0x200000018
mtime = Sat Jun 02 16:18:18 UTC 2018
pZxid = 0x20000001d
cversion = 2
dataVersion = 0
aclVersion = 3
ephemeralOwner = 0x0
dataLength = 4
numChildren = 0
# 获取 设置都提示权限不足
[zk: localhost:2181(CONNECTED) 49] get /merryyou/test
Authentication is not valid : /merryyou/test
[zk: localhost:2181(CONNECTED) 50] set /merryyou/test 123
Authentication is not valid : /merryyou/test
[zk: localhost:2181(CONNECTED) 51] 
```
#####  acl Auth 密码明文设置

```shell 
[zk: localhost:2181(CONNECTED) 53] create /niocoder/merryyou merryyou
Created /niocoder/merryyou
#查询默认节点权限信息
[zk: localhost:2181(CONNECTED) 54] getAcl /niocoder/merryyou
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 55] 
#使用auth设置节点权限信息
[zk: localhost:2181(CONNECTED) 2] setAcl /niocoder/merryyou auth:test:test:cdrwa  
Acl is not valid : /niocoder/merryyou
# 注册test:test 账号密码
[zk: localhost:2181(CONNECTED) 3] addauth digest test:test
[zk: localhost:2181(CONNECTED) 4] setAcl /niocoder/merryyou auth:test:test:cdrwa
cZxid = 0x200000020
ctime = Sat Jun 02 16:32:08 UTC 2018
mZxid = 0x200000020
mtime = Sat Jun 02 16:32:08 UTC 2018
pZxid = 0x200000020
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 8
numChildren = 0
# 查询节点权限信息 密码为密文格式
[zk: localhost:2181(CONNECTED) 5] getAcl /niocoder/merryyou
'digest,'test:V28q/NynI4JI3Rk54h0r8O5kMug=
: cdrwa
[zk: localhost:2181(CONNECTED) 6] 
```
#####  acl digest 密码密文设置
```shell 
[zk: localhost:2181(CONNECTED) 13] create /names test
Created /names
[zk: localhost:2181(CONNECTED) 14] getAcl /names
'world,'anyone
: cdrwa
#使用digest设置节点的权限信息 密码为test密文
[zk: localhost:2181(CONNECTED) 15] setAcl /names digest:test:V28q/NynI4JI3Rk54h0r8O5kMug=:cdra
cZxid = 0x400000006
ctime = Sun Jun 03 01:01:17 UTC 2018
mZxid = 0x400000006
mtime = Sun Jun 03 01:01:17 UTC 2018
pZxid = 0x400000006
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 4
numChildren = 0
#查询节点权限信息
[zk: localhost:2181(CONNECTED) 16] getAcl /names
'digest,'test:V28q/NynI4JI3Rk54h0r8O5kMug=
: cdra
#获取节点信息提示权限不足
[zk: localhost:2181(CONNECTED) 5] get /names
Authentication is not valid : /names
# 注册账户
[zk: localhost:2181(CONNECTED) 4] addauth digest test:test
# 可以正常获取
[zk: localhost:2181(CONNECTED) 17] get /names          
test
cZxid = 0x400000006
ctime = Sun Jun 03 01:01:17 UTC 2018
mZxid = 0x400000006
mtime = Sun Jun 03 01:01:17 UTC 2018
pZxid = 0x400000006
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 4
numChildren = 0
#由于没有设置写权限不能修改节点 w
[zk: localhost:2181(CONNECTED) 18] set /names 111
Authentication is not valid : /names
[zk: localhost:2181(CONNECTED) 19] delete /names
[zk: localhost:2181(CONNECTED) 20] 
```

#####  acl ip 控制客户端
```shell
[zk: localhost:2181(CONNECTED) 22] create /niocoder/ip aa
Created /niocoder/ip
[zk: localhost:2181(CONNECTED) 23] get /niocoder/ip
aa
cZxid = 0x40000000a
ctime = Sun Jun 03 01:06:47 UTC 2018
mZxid = 0x40000000a
mtime = Sun Jun 03 01:06:47 UTC 2018
pZxid = 0x40000000a
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 2
numChildren = 0
# 添加ip控制的权限信息
[zk: localhost:2181(CONNECTED) 24] setAcl /niocoder/ip ip:192.168.0.68:cdrwa
cZxid = 0x40000000a
ctime = Sun Jun 03 01:06:47 UTC 2018
mZxid = 0x40000000a
mtime = Sun Jun 03 01:06:47 UTC 2018
pZxid = 0x40000000a
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 2
numChildren = 0
[zk: localhost:2181(CONNECTED) 25] getAcl /niocoder/ip
'ip,'192.168.0.68
: cdrwa
[zk: localhost:2181(CONNECTED) 26] 
```
#####  acl super超级管理员
使用`super`权限需要修改`zkServer.sh`，添加`super`管理员，重启`zkServer.sh`
```shell 
"-Dzookeeper.DigestAuthenticationProvider.superDigest=test:V28q/NynI4JI3Rk54h0r8O5kMug="
 nohup "$JAVA" "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" "-Dzookeeper.DigestAuthenticationprovider.superDigest=test:V28q/NynI4JI3Rk54h0r8O5kMug=" \
    -cp "$CLASSPATH" $JVMFLAGS $ZOOMAIN "$ZOOCFG" > "$_ZOO_DAEMON_OUT" 2>&1 < /dev/null &

#重启进入zkCli
#由于之前设置ip权限，所以不允许访问
[zk: localhost:2181(CONNECTED) 2] ls /niocoder/ip
Authentication is not valid : /niocoder/ip
#登录账号信息，即为管理员账号
[zk: localhost:2181(CONNECTED) 3] addauth digest test:test
#正常访问，节点内容为空
[zk: localhost:2181(CONNECTED) 4] ls /niocoder/ip
[]
[zk: localhost:2181(CONNECTED) 5] get /niocoder/ip
aa
cZxid = 0x40000000a
ctime = Sun Jun 03 01:06:47 UTC 2018
mZxid = 0x40000000a
mtime = Sun Jun 03 01:06:47 UTC 2018
pZxid = 0x40000000a
cversion = 0
dataVersion = 0
aclVersion = 1
ephemeralOwner = 0x0
dataLength = 2
numChildren = 0
[zk: localhost:2181(CONNECTED) 6] 
```
#### 四字命令Four Letter Words
- 使用四字命令需要安装`nc`命令,(`yum install nc`)
##### stat 查看状态信息 
```shell
[root@localhost bin]# echo stat | nc 192.168.0.68 2181
Zookeeper version: 3.4.11-37e277162d567b55a07d1755f0b31c32e93c01a0, built on 11/01/2017 18:06 GMT
Clients:
 /192.168.0.68:49346[0](queued=0,recved=1,sent=0)

Latency min/avg/max: 0/0/4
Received: 62
Sent: 61
Connections: 1
Outstanding: 0
Zxid: 0x50000000a
Mode: follower
Node count: 10
[root@localhost bin]# 
```
##### ruok 查看zookeeper是否启动
```shell 
[root@localhost bin]# echo ruok | nc 192.168.0.68 2181
imok[root@localhost bin]# 
```
##### dump 列出没有处理的节点，临时节点 

```shell 
imok[root@localhost bin]# echo dump | nc 192.168.0.68 2181
SessionTracker dump:
org.apache.zookeeper.server.quorum.LearnerSessionTracker@29805957
ephemeral nodes dump:
Sessions with Ephemerals (0):
[root@localhost bin]# 
```
##### conf 查看服务器配置
```shell 
[root@localhost bin]# echo conf | nc 192.168.0.68 2181
clientPort=2181
dataDir=/usr/home/zookeeper-3.4.11/data/version-2
dataLogDir=/usr/home/zookeeper-3.4.11/data/version-2
tickTime=2000
maxClientCnxns=60
minSessionTimeout=4000
maxSessionTimeout=40000
serverId=2
initLimit=10
syncLimit=5
electionAlg=3
electionPort=3888
quorumPort=2888
peerType=0
[root@localhost bin]# 
```
##### cons 显示连接到服务端的信息
```shell 
[root@localhost bin]# echo cons | nc 192.168.0.68 2181
 /192.168.0.68:49354[0](queued=0,recved=1,sent=0)

[root@localhost bin]# 
```
##### envi 显示环境变量信息
```shell 
[root@localhost bin]# echo envi | nc 192.168.0.68 2181
Environment:
zookeeper.version=3.4.11-37e277162d567b55a07d1755f0b31c32e93c01a0, built on 11/01/2017 18:06 GMT
host.name=localhost
java.version=1.8.0_111
java.vendor=Oracle Corporation
java.home=/usr/local/jdk1.8.0_111/jre
java.class.path=/usr/home/zookeeper-3.4.11/bin/../build/classes:/usr/home/zookeeper-3.4.11/bin/../build/lib/*.jar:/usr/home/zookeeper-3.4.11/bin/../lib/slf4j-log4j12-1.6.1.jar:/usr/home/zookeeper-3.4.11/bin/../lib/slf4j-api-1.6.1.jar:/usr/home/zookeeper-3.4.11/bin/../lib/netty-3.10.5.Final.jar:/usr/home/zookeeper-3.4.11/bin/../lib/log4j-1.2.16.jar:/usr/home/zookeeper-3.4.11/bin/../lib/jline-0.9.94.jar:/usr/home/zookeeper-3.4.11/bin/../lib/audience-annotations-0.5.0.jar:/usr/home/zookeeper-3.4.11/bin/../zookeeper-3.4.11.jar:/usr/home/zookeeper-3.4.11/bin/../src/java/lib/*.jar:/usr/home/zookeeper-3.4.11/bin/../conf:
java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
java.io.tmpdir=/tmp
java.compiler=<NA>
os.name=Linux
os.arch=amd64
os.version=3.10.0-514.10.2.el7.x86_64
user.name=root
user.home=/root
user.dir=/usr/home/zookeeper-3.4.11/bin
[root@localhost bin]#
```
##### mntr 查看zk的健康信息
```shell 
[root@localhost bin]# echo mntr | nc 192.168.0.68 2181
zk_version	3.4.11-37e277162d567b55a07d1755f0b31c32e93c01a0, built on 11/01/2017 18:06 GMT
zk_avg_latency	0
zk_max_latency	4
zk_min_latency	0
zk_packets_received	68
zk_packets_sent	67
zk_num_alive_connections	1
zk_outstanding_requests	0
zk_server_state	follower
zk_znode_count	10
zk_watch_count	0
zk_ephemerals_count	0
zk_approximate_data_size	124
zk_open_file_descriptor_count	32
zk_max_file_descriptor_count	4096
[root@localhost bin]# 
```
##### wchs 展示watch的信息
```shell 
[root@localhost bin]# echo wchs | nc 192.168.0.68 2181
0 connections watching 0 paths
Total watches:0
[root@localhost bin]# 

```
##### wchc和wchp 显示session的watch信息 path的watch信息 
- 需要在 配置`zoo.cfg`文件中添加 `4lw.commands.whitelist=*`
```shell
[root@localhost bin]# echo wchc | nc 192.168.0.68 2181
wchc is not executed because it is not in the whitelist.
[root@localhost bin]# echo wchp | nc 192.168.0.68 2181
wchp is not executed because it is not in the whitelist.
```

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