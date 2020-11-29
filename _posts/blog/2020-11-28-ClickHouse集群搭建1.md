---
layout: post
title: ClickHouse集群搭建（一）
categories: ClickHouse
description: ClickHouse
keywords: ClickHouse
---
> 满目山河空念远，落花风雨更伤春。

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java42.jpg)


## `ClickHouse`概述

###  什么是`ClickHouse`?

`ClickHouse` 是俄罗斯的`Yandex`于2016年开源的列式存储数据库`（DBMS）`，主要用于在线分析处理查询`（OLAP）`，能够使用`SQL`查询实时生成分析数据报告。

### 什么是列式存储 ?

以下面表为例

| `id`  | `website`  | `wechat`  |
| ------------ | ------------ | ------------ |
| 1  | `https://niocoder.com/`  | `java干货`  |
| 2  | `http://www.merryyou.cn/`  |  `javaganhuo` |

采用行式存储时，数据在磁盘上的组织结构为：

| `Row1`  |   |   | `Row2`  |   |   |
| ------------ | ------------ | ------------ | ------------ | ------------ | ------------ |
| 1  | `https://niocoder.com/`  |  `java干货` |  2 | `http://www.merryyou.cn/`  |  `javaganhuo` |


好处是想查某条记录所有的属性时，可以通过一次磁盘查找加顺序读取就可以。但是当想查所有记录`wechat`时，需要不停的查找，或者全表扫描才行，遍历的很多数据都是不需要的。

而采用列式存储时，数据在磁盘上的组织结构为：

| `col1`  |   | `col2`  |   | `col3`  |   |
| ------------ | ------------ | ------------ | ------------ | ------------ | ------------ |
| 1  | 2  |  `https://niocoder.com/` |  `http://www.merryyou.cn/` | `java干货`  |  `javaganhuo` |

这时想查所有记录的`wechat`只需把`col3`那一列拿出来即可。

## 集群环境搭建

在 安装`ClickHouse` 具体开始前, 先来搭建一下环境,软件包下载见末尾。

### 创建虚拟机
#### 安装虚拟机 `VMWare`

安装`Vmware虚拟机`

#### 导入 `CentOS`

将下载的`CentOS`系统中导入到 `VMWare`中

**注意事项**：windows系统确认所有的关于VmWare的服务都已经启动，

**确认好`VmWare`生成的网关地址，另外确认VmNet8网卡已经配置好了IP地址。**

更多关于`VmWare`网络模式参考[VMware虚拟机三种网络模式详解](https://mp.weixin.qq.com/s/p9K7WGvRUGW79eE800yLEg)

#### 集群规划

| IP              | 主机名 | 环境配置                              | 安装    | `ClickHouse`                                  |
| --------------- | ------ | ------------------------------------- | ----------------------------------------- | ------ |
| `192.168.10.100` | `node01` | 关防火墙, `host`映射, 时钟同步 | `JDK`,  `Zookeeper` | `clickhouse-server 9000` `clickhouse-server 9001` |
| `192.168.10.110` | `node02` | 关防火墙, `host`映射, 时钟同步 | `JDK`,  `Zookeeper` | `clickhouse-server 9000` `clickhouse-server 9001` |
| `192.168.10.120` | `node03` | 关防火墙, `host`映射, 时钟同步 | `JDK`, `Zookeeper` | `clickhouse-server 9000` `clickhouse-server 9001` |

### 配置每台主机

更改`ip`地址和`HWADDR`地址

`vim /etc/sysconfig/network-scripts/ifcfg-eth0`

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/ch01.png)

`HWADDR`地址查看

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/ch04.png)

修改主机名(重启后永久生效)

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/ch02.png)

`vim /etc/hostname`

设置`hosts`域名映射

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/ch03.png)

  `vim /etc/hosts`

#### 关闭防火墙

三台机器执行以下命令

`systemctl status firewalld.service  #查看防火墙状态`
`systemctl stop firewalld.service  #关闭防火墙`
`systemctl disable firewalld.service #永久关闭防火墙`

#### 免密登录

为了方便传输文件,三台机器之前配置免密登录.

- **免密 SSH 登录的原理**
  1. 需要先在 B节点 配置 A节点 的公钥
  2. A节点 请求 B节点 要求登录
  3. B节点 使用 A节点 的公钥, 加密一段随机文本
  4. A节点 使用私钥解密, 并发回给 B节点
  5. B节点 验证文本是否正确

##### 三台机器生成公钥与私钥

在三台机器执行以下命令，生成公钥与私钥

`ssh-keygen -t rsa`

执行该命令之后，按下三个回车即可

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/ch05.png)

##### 拷贝公钥到`node01`机器

三台机器将拷贝公钥到`node01`机器

三台机器执行命令：

`ssh-copy-id node01`

##### 复制`node01`机器的认证到其他机器

将第一台机器的公钥拷贝到其他机器上

在`node01`机器上面执行以下命令

`scp /root/.ssh/authorized_keys node02:/root/.ssh`

`scp /root/.ssh/authorized_keys node03:/root/.ssh`

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/ch06.png)

#### 设置时钟同步服务

- 为什么需要时间同步
  - 因为很多分布式系统是有状态的, 比如说存储一个数据, A节点 记录的时间是 1, B节点 记录的时间是 2, 就会出问题

```shell
## 安装
yum install -y ntp

## 启动定时任务
crontab -e
```

随后在输入界面键入

```shell
*/1 * * * * /usr/sbin/ntpdate ntp4.aliyun.com;
```

####  安装`JDK`

上传`jdk`并解压然后配置环境变量

所有软件的安装路径

`mkdir -p /export/servers #node01,node02,node03机器上执行`

所有软件压缩包的存放路径

`mkdir -p /export/softwares # 创建文件夹`

安装`rz`和`sz`命令

`yum -y install lrzsz # 安装rz sz 命令`

上传`jdk`安装包到`/export/softwares`路径下去，并解压

`tar -zxvf jdk-8u141-linux-x64.tar.gz -C ../servers/`

配置环境变量

`vim /etc/profile`

```shell
export JAVA_HOME=/export/servers/jdk1.8.0_141
export PATH=:$JAVA_HOME/bin:$PATH
```

执行以下命令将`jdk`安装包分发到`node02`和`node03`节点上

`scp -r  /export/servers/jdk1.8.0_141/ node02:/export/servers/`
`scp -r  /export/servers/jdk1.8.0_141/ node03:/export/servers/`
`scp /etc/profile node02:/etc/profile`
`scp /etc/profile node03:/etc/profile`

刷新环境变量

`source /etc/profile`

####  安装`Zookeeper`

| 服务器IP        | 主机名 | myid的值 |
| --------------- | ------ | -------- |
| 192.168.10.100 | node01 | 1        |
| 192.168.10.110 | node02 | 2        |
| 192.168.10.120 | node03 | 3        |

上传`zookeeper`安装包到`/export/softwares`路径下去，并解压

`tar -zxvf zookeeper-3.4.9.tar.gz -C ../servers/ `

`node01`修改配置文件如下

```shell
cd /export/servers/zookeeper-3.4.9/conf/

cp zoo_sample.cfg zoo.cfg
# 创建数据存放节点
mkdir -p /export/servers/zookeeper-3.4.9/zkdatas/
```

`vim  zoo.cfg`

```shell
dataDir=/export/servers/zookeeper-3.4.9/zkdatas
# 保留多少个快照
autopurge.snapRetainCount=3
# 日志多少小时清理一次
autopurge.purgeInterval=1
# 集群中服务器地址
server.1=node01:2888:3888
server.2=node02:2888:3888
server.3=node03:2888:3888
```

在`node01`机器上

`/export/servers/zookeeper-3.4.9/zkdatas/`这个路径下创建一个文件，文件名为`myid` ,文件内容为`1`

`echo 1 > /export/servers/zookeeper-3.4.9/zkdatas/myid` 

安装包分发到其他机器

`node01`机器上面执行以下两个命令

`scp -r  /export/servers/zookeeper-3.4.9/ node02:/export/servers/`

`scp -r  /export/servers/zookeeper-3.4.9/ node03:/export/servers/`

`node02`机器上修改`myid`的值为`2`

`echo 2 > /export/servers/zookeeper-3.4.9/zkdatas/myid`

`node03`机器上修改`myid`的值为`3`

`echo 3 > /export/servers/zookeeper-3.4.9/zkdatas/myid`

启动`zookeeper`服务

`node01`,`node02`,`node03`机器都要执行

`/export/servers/zookeeper-3.4.9/bin/zkServer.sh start`

查看启动状态

`/export/servers/zookeeper-3.4.9/bin/zkServer.sh  status`

### 安装前准备

####  CPU是否支持SSE4.2

查看CPU是否支持SSE4.2指令集

`grep -q sse4_2 /proc/cpuinfo && echo "SSE 4.2 supported" || echo "SSE 4.2 not supported"
`

#### 安装必要依赖

`yum install -y unixODBC libicudata `

`yum install -y libxml2-devel expat-devel libicu-devel`

### 安装`ClickHouse`

#### 单机模式

##### 上传4个文件到`node01`机器`/opt/software/`

```shell
[root@node01 softwares]# ll
total 306776
-rw-r--r--. 1 root root      6384 Nov  2 22:43 clickhouse-client-20.8.3.18-1.el7.x86_64.rpm
-rw-r--r--. 1 root root  69093220 Nov  2 22:48 clickhouse-common-static-20.8.3.18-1.el7.x86_64.rpm
-rw-r--r--. 1 root root  36772044 Nov  2 22:51 clickhouse-server-20.8.3.18-1.el7.x86_64.rpm
-rw-r--r--. 1 root root     14472 Nov  2 22:43 clickhouse-server-common-20.8.3.18-1.el7.x86_64.rpm
```

##### 分别安装这4个`rpm`安装包

```shell
[root@node01 softwares]# rpm -ivh clickhouse-common-static-20.8.3.18-1.el7.x86_64.rpm 
Preparing...                          ################################# [100%]
Updating / installing...
   1:clickhouse-common-static-20.8.3.1################################# [100%]
[root@node01 softwares]# rpm -ivh clickhouse-server-common-20.8.3.18-1.el7.x86_64.rpm 
Preparing...                          ################################# [100%]
Updating / installing...
   1:clickhouse-server-common-20.8.3.1################################# [100%]
[root@node01 softwares]# rpm -ivh clickhouse-server-20.8.3.18-1.el7.x86_64.rpm 
Preparing...                          ################################# [100%]
Updating / installing...
   1:clickhouse-server-20.8.3.18-1.el7################################# [100%]
Create user clickhouse.clickhouse with datadir /var/lib/clickhouse
[root@node01 softwares]# rpm -ivh clickhouse-client-20.8.3.18-1.el7.x86_64.rpm 
Preparing...                          ################################# [100%]
Updating / installing...
   1:clickhouse-client-20.8.3.18-1.el7################################# [100%]
Create user clickhouse.clickhouse with datadir /var/lib/clickhouse
```

##### `rpm`安装完毕后，`clickhouse-server`和`clickhouse-client`配置目录如下

```shell 
[root@node01 softwares]# ll /etc/clickhouse-server/
total 44
-rw-r--r--. 1 root root 33738 Oct  6 06:05 config.xml
-rw-r--r--. 1 root root  5587 Oct  6 06:05 users.xml
[root@node01 softwares]# ll /etc/clickhouse-client/
total 4
drwxr-xr-x. 2 clickhouse clickhouse    6 Nov 28 22:19 conf.d
-rw-r--r--. 1 clickhouse clickhouse 1568 Oct  6 04:44 config.xml

```

##### `/etc/clickhouse-server/`的`config.xml`为`ClickHouse`核心配置文件,主要内容如下

```xml
<?xml version="1.0"?>
<yandex>
   <!-- 日志 -->
   <logger>
       <level>trace</level>
       <log>/data1/clickhouse/log/server.log</log>
       <errorlog>/data1/clickhouse/log/error.log</errorlog>
       <size>1000M</size>
       <count>10</count>
   </logger>

   <!-- 端口 -->
   <http_port>8123</http_port>
   <tcp_port>9000</tcp_port>
   <interserver_http_port>9009</interserver_http_port>

   <!-- 本机域名 -->
   <interserver_http_host>这里需要用域名，如果后续用到复制的话</interserver_http_host>

   <!-- 监听IP -->
   <listen_host>0.0.0.0</listen_host>
   <!-- 最大连接数 -->
   <max_connections>64</max_connections>

   <!-- 没搞懂的参数 -->
   <keep_alive_timeout>3</keep_alive_timeout>

   <!-- 最大并发查询数 -->
   <max_concurrent_queries>16</max_concurrent_queries>

   <!-- 单位是B -->
   <uncompressed_cache_size>8589934592</uncompressed_cache_size>
   <mark_cache_size>10737418240</mark_cache_size>

   <!-- 存储路径 -->
   <path>/data1/clickhouse/</path>
   <tmp_path>/data1/clickhouse/tmp/</tmp_path>

   <!-- user配置 -->
   <users_config>users.xml</users_config>
   <default_profile>default</default_profile>

   <log_queries>1</log_queries>

   <default_database>default</default_database>

   <remote_servers incl="clickhouse_remote_servers" />
   <zookeeper incl="zookeeper-servers" optional="true" />
   <macros incl="macros" optional="true" />

   <!-- 没搞懂的参数 -->
   <builtin_dictionaries_reload_interval>3600</builtin_dictionaries_reload_interval>

   <!-- 控制大表的删除 -->
   <max_table_size_to_drop>0</max_table_size_to_drop>

   <include_from>/data1/clickhouse/metrika.xml</include_from>
</yandex>
````

##### 启动`ClickHouse`

```shell
[root@node01 softwares]# service clickhouse-server start
Start clickhouse-server service: Path to data directory in /etc/clickhouse-server/config.xml: /var/lib/clickhouse/
DONE
[root@node01 softwares]# 
```

#### 使用`client`链接`server`

```shell
[root@node01 softwares]# clickhouse-client -m
ClickHouse client version 20.8.3.18.
Connecting to localhost:9000 as user default.
Connected to ClickHouse server version 20.8.3 revision 54438.

node01 :) show databases;

SHOW DATABASES

┌─name───────────────────────────┐
│ _temporary_and_external_tables │
│ default                        │
│ system                         │
└────────────────────────────────┘

3 rows in set. Elapsed: 0.007 sec. 

node01 :) select 1;

SELECT 1

┌─1─┐
│ 1 │
└───┘

1 rows in set. Elapsed: 0.005 sec. 

node01 :) 
```

#### 分布式集群安装

#####  在`node02`，`node03`上面执行之前的所有的操作 

##### `node01`机器修改配置文件`config.xml`

```shell
[root@node01 softwares]# vim /etc/clickhouse-server/config.xml
<!-- 打开这个 -->
 <listen_host>::</listen_host>
    <!-- Same for hosts with disabled ipv6: -->
    <!-- <listen_host>0.0.0.0</listen_host> -->
    <!-- 新增外部配置文件metrika.xml  -->
<include_from>/etc/clickhouse-server/metrika.xml</include_from>

```

将修改后的配置分发到`node02`,`node03`机器上

`scp config.xml node02:/etc/clickhouse-server/config.xml`

`scp config.xml node03:/etc/clickhouse-server/config.xml`

##### `node01`机器`/etc/clickhouse-server/`目录下创建`metrika.xml`文件

```xml
<yandex>
<!-- 集群配置 -->
<clickhouse_remote_servers>
    <!-- 3分片1备份 -->
    <cluster_3shards_1replicas>
        <!-- 数据分片1  -->
        <shard>
            <replica>
                <host>node01</host>
                <port>9000</port>
            </replica>
        </shard>
        <!-- 数据分片2  -->
        <shard>
            <replica>
                <host>node02</host>
                <port> 9000</port>
            </replica>
        </shard>
        <!-- 数据分片3  -->
        <shard>
            <replica>
                <host>node03</host>
                <port>9000</port>
            </replica>
        </shard>
    </cluster_3shards_1replicas>
</clickhouse_remote_servers>
</yandex>
```
配置说明

- `cluster_3shards_1replicas` 集群名称,可随意定义
-  共设置3个分片，每个分片只有1个副本；

将`metrika.xml`配置文件分发到`node02`,`node03`机器上

`scp metrika.xml node02:/etc/clickhouse-server/metrika.xml`

`scp metrika.xml node03:/etc/clickhouse-server/metrika.xml`

##### 重启`ClickHouse-server`  打开`client`查看集群

```shell
[root@node01 clickhouse-server]# service clickhouse-server restart
Stop clickhouse-server service: DONE
Start clickhouse-server service: Path to data directory in /etc/clickhouse-server/config.xml: /var/lib/clickhouse/
DONE
[root@node01 clickhouse-server]# clickhouse-client -m
ClickHouse client version 20.8.3.18.
Connecting to localhost:9000 as user default.
Connected to ClickHouse server version 20.8.3 revision 54438.

node01 :) select * from system.clusters;

SELECT *
FROM system.clusters

┌─cluster───────────────────────────┬─shard_num─┬─shard_weight─┬─replica_num─┬─host_name─┬─host_address───┬─port─┬─is_local─┬─user────┬─default_database─┬─errors_count─┬─estimated_recovery_time─┐
│ cluster_3shards_1replicas         │         1 │            1 │           1 │ node01    │ 192.168.10.100 │ 9000 │        1 │ default │                  │            0 │                       0 │
│ cluster_3shards_1replicas         │         2 │            1 │           1 │ node02    │ 192.168.10.110 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ cluster_3shards_1replicas         │         3 │            1 │           1 │ node03    │ 192.168.10.120 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         1 │            1 │           1 │ 127.0.0.1 │ 127.0.0.1      │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         2 │            1 │           1 │ 127.0.0.2 │ 127.0.0.2      │ 9000 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         2 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost              │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost_secure       │         1 │            1 │           1 │ localhost │ ::1            │ 9440 │        0 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         2 │            1 │           1 │ localhost │ ::1            │    1 │        0 │ default │                  │            0 │                       0 │
└───────────────────────────────────┴───────────┴──────────────┴─────────────┴───────────┴────────────────┴──────┴──────────┴─────────┴──────────────────┴──────────────┴─────────────────────────┘

11 rows in set. Elapsed: 0.008 sec.                │            0 │                      
```

可以看到`cluster_3shards_1replicas`就是我们定义的集群名称，一共有三个分片，每个分片有一份数据。剩下的为配置文件默认自带的集群配置.

##### 测试分布式集群

在`node01`,`node02`,`node03`上分别创建本地表`cluster3s1r_local`

```sql
CREATE TABLE default.cluster3s1r_local
(
	`id` Int32,
    `website` String,
    `wechat` String,
	`FlightDate` Date,
	Year UInt16
)
ENGINE = MergeTree(FlightDate, (Year, FlightDate), 8192);
```

在`node01`节点上创建分布式表

```sql
CREATE TABLE default.cluster3s1r_all AS cluster3s1r_local
ENGINE = Distributed(cluster_3shards_1replicas, default, cluster3s1r_local, rand());
```
往分布式表`cluster3s1r_all`插入数据,`cluster3s1r_all `会随机插入到三个节点的`cluster3s1r_local`里

插入数据

```sql
INSERT INTO default.cluster3s1r_all (id,website,wechat,FlightDate,Year)values(1,'https://niocoder.com/','java干货','2020-11-28',2020);
INSERT INTO default.cluster3s1r_all (id,website,wechat,FlightDate,Year)values(2,'http://www.merryyou.cn/','javaganhuo','2020-11-28',2020);
INSERT INTO default.cluster3s1r_all (id,website,wechat,FlightDate,Year)values(3,'http://www.xxxxx.cn/','xxxxx','2020-11-28',2020);
```

查询分布式表和本地表

```shell
node01 :) select * from cluster3s1r_all; # 查询总量查分布式表

SELECT *
FROM cluster3s1r_all

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
┌─id─┬─website──────────────┬─wechat─┬─FlightDate─┬─Year─┐
│  3 │ http://www.xxxxx.cn/ │ xxxxx  │ 2020-11-28 │ 2020 │
└────┴──────────────────────┴────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

3 rows in set. Elapsed: 0.036 sec. 

node01 :) select * from cluster3s1r_local; # node01本地表

SELECT *
FROM cluster3s1r_local

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘

1 rows in set. Elapsed: 0.012 sec.

node02 :) select * from cluster3s1r_local; # node02本地表

SELECT *
FROM cluster3s1r_local

Ok.

0 rows in set. Elapsed: 0.016 sec. 

node03 :) select * from cluster3s1r_local; ## node03 本地表

SELECT *
FROM cluster3s1r_local

┌─id─┬─website──────────────┬─wechat─┬─FlightDate─┬─Year─┐
│  3 │ http://www.xxxxx.cn/ │ xxxxx  │ 2020-11-28 │ 2020 │
└────┴──────────────────────┴────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

2 rows in set. Elapsed: 0.006 sec. 

```

## 下载

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/gzh.png)

> 关注微信公众号java干货回复 【clickhouse】
