---
layout: post
title: ClickHouse集群搭建（二）
categories: ClickHouse
description: ClickHouse
keywords: ClickHouse
---
> 重叠泪痕缄锦字，人生只有情难死。

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java43.jpg)


### 分布式集群安装

在上一章我们已经完成`ClickHouse`分布式集群安装，也创建本地表和分布式表进行了测试，但是，假如停掉一个节点会发生神马情况？

#### `node03`上`kill`掉`clickhouse-server`进程

```shell
[root@node03 ~]# ps -ef | grep clickhouse
clickho+  2233     1 73 13:07 ?        00:00:02 clickhouse-server --daemon --pid-file=/var/run/clickhouse-server/clickhouse-server.pid --config-file=/etc/clickhouse-server/config.xml
root      2306  1751  0 13:07 pts/0    00:00:00 grep --color=auto clickhouse
[root@node03 ~]# service clickhouse-server stop
Stop clickhouse-server service: DONE
[root@node03 ~]# ps -ef | grep clickhouse
root      2337  1751  0 13:07 pts/0    00:00:00 grep --color=auto clickhouse
```
#### `node01`上查询分布式表

```shell
node01 :) select * from cluster3s1r_all; # node03没有被杀掉时

SELECT *
FROM cluster3s1r_all

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘
┌─id─┬─website──────────────┬─wechat─┬─FlightDate─┬─Year─┐
│  3 │ http://www.xxxxx.cn/ │ xxxxx  │ 2020-11-28 │ 2020 │
└────┴──────────────────────┴────────┴────────────┴──────┘

3 rows in set. Elapsed: 0.037 sec. 

node01 :) select * from cluster3s1r_all; # node03节点被杀掉时

SELECT *
FROM cluster3s1r_all

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
↘ Progress: 1.00 rows, 59.00 B (8.87 rows/s., 523.62 B/s.)  0%
Received exception from server (version 20.8.3):
Code: 279. DB::Exception: Received from localhost:9000. DB::Exception: All connection tries failed. Log: 

Code: 32, e.displayText() = DB::Exception: Attempt to read after eof (version 20.8.3.18)
Code: 210, e.displayText() = DB::NetException: Connection refused (node03:9000) (version 20.8.3.18)
Code: 210, e.displayText() = DB::NetException: Connection refused (node03:9000) (version 20.8.3.18)

: While executing Remote. 

1 rows in set. Elapsed: 0.114 sec. 

```

只返回了`node01`节点上的数据，`node03`节点上的两条数据丢失。


### 数据备份

但在`ClickHouse`中，`replica`是挂在`shard`上的，因此要用多副本，必须先定义`shard`。

最简单的情况：1个分片多个副本。

#### 修改`metrika.xml`文件

`node01` 上修改 `/etc/clickhouse-server/metrika.xml`集群配置文件

```xml
<yandex>
<!-- 集群配置 -->
<clickhouse_remote_servers>
    <!-- 1分片2备份 -->
    <cluster_1shards_2replicas>
        <!-- 数据分片1  -->
        <shard>
		<!-- false代表一次性写入所有副本，true表示写入其中一个副本，配合zk来进行数据复制 -->
		<internal_replication>false</internal_replication>
            <replica>
                <host>node01</host>
                <port>9000</port>
            </replica>
            <replica>
                <host>node02</host>
                <port>9000</port>
            </replica>	
		</shard>
    </cluster_1shards_2replicas>
</clickhouse_remote_servers>
</yandex>
```

将修改后的配置分发到`node02`机器上

```shell
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/metrika.xml node02:$PWD
metrika.xml                                                                                                                                                  100%  674   618.9KB/s   00:00    
```

如果配置文件没有问题，是不用重启`clickhouse-server`的，会自动加载配置文件，`node01`上查看集群信息

```shell
[root@node01 clickhouse-server]# clickhouse-client -m
ClickHouse client version 20.8.3.18.
Connecting to localhost:9000 as user default.
Connected to ClickHouse server version 20.8.3 revision 54438.

node01 :) select * from system.clusters;

SELECT *
FROM system.clusters

┌─cluster───────────────────────────┬─shard_num─┬─shard_weight─┬─replica_num─┬─host_name─┬─host_address───┬─port─┬─is_local─┬─user────┬─default_database─┬─errors_count─┬─estimated_recovery_time─┐
│ cluster_1shards_2replicas         │         1 │            1 │           1 │ node01    │ 192.168.10.100 │ 9000 │        1 │ default │                  │            0 │                       0 │
│ cluster_1shards_2replicas         │         1 │            1 │           2 │ node02    │ 192.168.10.110 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         1 │            1 │           1 │ 127.0.0.1 │ 127.0.0.1      │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         2 │            1 │           1 │ 127.0.0.2 │ 127.0.0.2      │ 9000 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         2 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost              │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost_secure       │         1 │            1 │           1 │ localhost │ ::1            │ 9440 │        0 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         2 │            1 │           1 │ localhost │ ::1            │    1 │        0 │ default │                  │            0 │                       0 │
└───────────────────────────────────┴───────────┴──────────────┴─────────────┴───────────┴────────────────┴──────┴──────────┴─────────┴──────────────────┴──────────────┴─────────────────────────┘

10 rows in set. Elapsed: 0.018 sec.
```

#### 测试数据备份

在`node01`和`node02`上分别创建本地表`cluster1s2r_local`

```sql
CREATE TABLE default.cluster1s2r_local
(
	`id` Int32,
    `website` String,
    `wechat` String,
	`FlightDate` Date,
	Year UInt16
)
ENGINE = MergeTree(FlightDate, (Year, FlightDate), 8192);
```

在`node01`机器上创建分布式表,注意集群名称

```sql
CREATE TABLE default.cluster1s2r_all AS cluster1s2r_local
ENGINE = Distributed(cluster_1shards_2replicas, default, cluster1s2r_local, rand());
```

往分布式表`cluster1s2r_all`插入数据,`cluster1s2r_all` 会全部插入到`node01`和`node02`节点的`cluster1s2r_local`里

插入数据

```sql
INSERT INTO default.cluster1s2r_all (id,website,wechat,FlightDate,Year)values(1,'https://niocoder.com/','java干货','2020-11-28',2020);
INSERT INTO default.cluster1s2r_all (id,website,wechat,FlightDate,Year)values(2,'http://www.merryyou.cn/','javaganhuo','2020-11-28',2020);
INSERT INTO default.cluster1s2r_all (id,website,wechat,FlightDate,Year)values(3,'http://www.xxxxx.cn/','xxxxx','2020-11-28',2020);
```

查询分布式表和本地表

```sql
node01 :) select * from cluster1s2r_all; # 查询分布式表

SELECT *
FROM cluster1s2r_all

┌─id─┬─website──────────────┬─wechat─┬─FlightDate─┬─Year─┐
│  3 │ http://www.xxxxx.cn/ │ xxxxx  │ 2020-11-28 │ 2020 │
└────┴──────────────────────┴────────┴────────────┴──────┘
┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

3 rows in set. Elapsed: 0.018 sec. 

node01 :) select * from cluster1s2r_local; # node01节点查询本地表

SELECT *
FROM cluster1s2r_local

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘
┌─id─┬─website──────────────┬─wechat─┬─FlightDate─┬─Year─┐
│  3 │ http://www.xxxxx.cn/ │ xxxxx  │ 2020-11-28 │ 2020 │
└────┴──────────────────────┴────────┴────────────┴──────┘

3 rows in set. Elapsed: 0.015 sec. 

node02 :)  select * from cluster1s2r_local;  # node02节点查询本地表

SELECT *
FROM cluster1s2r_local

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘
┌─id─┬─website──────────────┬─wechat─┬─FlightDate─┬─Year─┐
│  3 │ http://www.xxxxx.cn/ │ xxxxx  │ 2020-11-28 │ 2020 │
└────┴──────────────────────┴────────┴────────────┴──────┘

3 rows in set. Elapsed: 0.007 sec. 

```

查询`node01`和`node02`本地表`cluster1s2r_local`都是全量数据， 即使`sotp`到其中一个节点数据也不会丢失，数据副本已经生效。

#### 数据副本一致性问题

既然有多副本，就有个一致性的问题：加入写入数据时，挂掉一台机器，会怎样？

模拟写入分布式表是某一个节点`down`机

1. 停掉`node02`节点服务
	`service clickhouse-server stop`
	
2. 在`node01`节点上向分布式表`cluster1s2r_all`插入数据

   ```sql INSERT INTO default.cluster1s2r_all (id,website,wechat,FlightDate,Year)values(4,'http://www.yyyyyy.cn/','yyyyy','2020-11-29',2020);```

3. 启动`node02`节点服务

4. 查询验证是否同步
	查看`node01`和`node02`机器的`cluster1s2r_local`、以及`cluster1s2r_all`，发现都是总数据量都增加了1条，说明这种情况下，集群节点之间能够自动同步

上面是通过向分布式表`cluster1s2r_all`插入数据，如果通过本地表`cluster1s2r_local`，数据还能同步吗？

1. 在`node01`上往`cluster1s2r_local`插入1条数据；
2. 查询`node02`，`cluster1s2r_local`数据没有同步

综上所述，通过分布表写入数据，会自动同步数据；而通过本地表表写入数据，不会同步；一般i情况下是没什么大问题。

**但是生产情况总比理论复杂的多，以上配置可能会存在数据不一致的问题** 

[官方文档](https://clickhouse.tech/docs/zh/engines/table-engines/special/distributed/)描述如下：

```shell
Each shard can have the internal_replication parameter defined in the config file.

If this parameter is set to true, the write operation selects the first healthy replica and writes data to it. Use this alternative if the Distributed table “looks at” replicated tables. In other words, if the table where data will be written is going to replicate them itself.

If it is set to false (the default), data is written to all replicas. In essence, this means that the Distributed table replicates data itself. This is worse than using replicated tables, because the consistency of replicas is not checked, and over time they will contain slightly different data.
```

翻译如下：

```shell
分片可在配置文件中定义 ‘internal_replication’ 参数。

此参数设置为«true»时，写操作只选一个正常的副本写入数据。如果分布式表的子表是复制表(*ReplicaMergeTree)，请使用此方案。换句话说，这其实是把数据的复制工作交给实际需要写入数据的表本身而不是分布式表。

若此参数设置为«false»（默认值），写操作会将数据写入所有副本。实质上，这意味着要分布式表本身来复制数据。这种方式不如使用复制表的好，因为不会检查副本的一致性，并且随着时间的推移，副本数据可能会有些不一样。
```

简单理解如下：

```shell
这个为true代表zk会挑选一个合适的节点写入,然后在后台进行多个节点之间数据的同步.
如果是false,则是一次性写入所有节点,以这种重复写入的方法实现节点之间数据的同步.
```

#### 自动数据备份

自动数据备份是表的行为，引擎为 `ReplicatedXXX`的表支持自动同步。

`Replicated`前缀只用于`MergeTree`系列（`MergeTree`是最常用的引擎）。

**重点说明：  `Replicated`表自动同步与之前的集群自动同步不同，是表的行为，与`metrika.xml`中的`<clickhouse_remote_servers>`配置没有关系，只要有`zookeeper`配置就行了。**

`node01`修改`metrika.xml`配置

```xml
<yandex>
<zookeeper-servers>
        <node index="1">
            <host>node01</host>
            <port>2181</port>
        </node>
        <node index="2">
            <host>node02</host>
            <port>2181</port>
        </node>
        <node index="3">
            <host>node03</host>
            <port>2181</port>
        </node>
    </zookeeper-servers>
</yandex>

```

将修改后的配置分发到`node02`机器上

```shell
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/metrika.xml node02:$PWD
metrika.xml   

重启`clickhouse-server`,由于之前的表存在导致启动是失败。`error`日志

​```shell
[root@node01 clickhouse-server]# tail -f /var/log/clickhouse-server/clickhouse-server.err.log 
7. DB::StorageDistributed::startup() @ 0x10f1bd40 in /usr/bin/clickhouse
8. ? @ 0x1151d922 in /usr/bin/clickhouse
9. ThreadPoolImpl<ThreadFromGlobalPool>::worker(std::__1::__list_iterator<ThreadFromGlobalPool, void*>) @ 0xa43d6ad in /usr/bin/clickhouse
10. ThreadFromGlobalPool::ThreadFromGlobalPool<void ThreadPoolImpl<ThreadFromGlobalPool>::scheduleImpl<void>(std::__1::function<void ()>, int, std::__1::optional<unsigned long>)::'lambda1'()>(void&&, void ThreadPoolImpl<ThreadFromGlobalPool>::scheduleImpl<void>(std::__1::function<void ()>, int, std::__1::optional<unsigned long>)::'lambda1'()&&...)::'lambda'()::operator()() const @ 0xa43dd93 in /usr/bin/clickhouse
11. ThreadPoolImpl<std::__1::thread>::worker(std::__1::__list_iterator<std::__1::thread, void*>) @ 0xa43cc4d in /usr/bin/clickhouse
12. ? @ 0xa43b3ff in /usr/bin/clickhouse
13. start_thread @ 0x7ea5 in /usr/lib64/libpthread-2.17.so
14. clone @ 0xfe8dd in /usr/lib64/libc-2.17.so
 (version 20.8.3.18)
2020.11.29 14:43:01.163530 [ 3643 ] {} <Error> Application: DB::Exception: Requested cluster 'cluster_1shards_2replicas' not found: while loading database `default` from path /var/lib/clickhouse/metadata/default

```

删除之前的建表语句

```shell
[root@node01 default]# rm -rf /var/lib/clickhouse/metadata/default/*.sql
```

启动`clickhouse-server`

在`node01`和`node02`节点上创建数据库表

```sql
-- node01 节点
CREATE TABLE `cluster_zk` 
	(
	`id` Int32,
    `website` String,
    `wechat` String,
	`FlightDate` Date,
	Year UInt16
)
 ENGINE = ReplicatedMergeTree('/clickhouse/tables/cluster_zk', 'replica01', FlightDate, (Year, FlightDate), 8192);

-- node02节点
CREATE TABLE `cluster_zk` 
	(
	`id` Int32,
    `website` String,
    `wechat` String,
	`FlightDate` Date,
	Year UInt16
)
ENGINE = ReplicatedMergeTree('/clickhouse/tables/cluster_zk', 'replica02', FlightDate, (Year, FlightDate), 8192);
```

`node01`节点上插入数据

```sql
INSERT INTO default.cluster_zk (id,website,wechat,FlightDate,Year)values(1,'https://niocoder.com/','java干货','2020-11-28',2020);
```

`node01`,`node02`节点上查询数据

```shell

node01 :) select * from cluster_zk; # node01节点

SELECT *
FROM cluster_zk

┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

1 rows in set. Elapsed: 0.004 sec. 

node02 :) select * from cluster_zk; # node02节点

SELECT *
FROM cluster_zk

┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

1 rows in set. Elapsed: 0.004 sec. 

```

查询`zk`信息 

```shell
[zk: localhost:2181(CONNECTED) 2] ls /clickhouse/tables/cluster_zk/replicas
[replica02, replica01]
[zk: localhost:2181(CONNECTED) 3]
```

#### 自动数据备份集群配置


`node01`修改`metrika.xml`配置, 注意此处`internal_replication`为`true`

```xml
<yandex>
<clickhouse_remote_servers>
    <perftest_1shards_2replicas>
                <shard>
                    <internal_replication>true</internal_replication>
                    <replica>
                        <host>node01</host>
                        <port>9000</port>
                    </replica>
                    <replica>
                        <host>node02</host>
                        <port>9000</port>
                    </replica>
                </shard>
    </perftest_1shards_2replicas>
</clickhouse_remote_servers>
<zookeeper-servers>
        <node index="1">
            <host>node01</host>
            <port>2181</port>
        </node>
        <node index="2">
            <host>node02</host>
            <port>2181</port>
        </node>
        <node index="3">
            <host>node03</host>
            <port>2181</port>
        </node>
    </zookeeper-servers>
</yandex>

```

将修改后的配置分发到`node02`机器上

```shell
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/metrika.xml node02:$PWD
metrika.xml   

```

查询集群信息

```sql
node01 :) select * from system.clusters;

SELECT *
FROM system.clusters

┌─cluster───────────────────────────┬─shard_num─┬─shard_weight─┬─replica_num─┬─host_name─┬─host_address───┬─port─┬─is_local─┬─user────┬─default_database─┬─errors_count─┬─estimated_recovery_time─┐
│ perftest_1shards_2replicas        │         1 │            1 │           1 │ node01    │ 192.168.10.100 │ 9000 │        1 │ default │                  │            0 │                       0 │
│ perftest_1shards_2replicas        │         1 │            1 │           2 │ node02    │ 192.168.10.110 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         1 │            1 │           1 │ 127.0.0.1 │ 127.0.0.1      │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         2 │            1 │           1 │ 127.0.0.2 │ 127.0.0.2      │ 9000 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         2 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost              │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost_secure       │         1 │            1 │           1 │ localhost │ ::1            │ 9440 │        0 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         2 │            1 │           1 │ localhost │ ::1            │    1 │        0 │ default │                  │            0 │                       0 │
└───────────────────────────────────┴───────────┴──────────────┴─────────────┴───────────┴────────────────┴──────┴──────────┴─────────┴──────────────────┴──────────────┴─────────────────────────┘

10 rows in set. Elapsed: 0.018 sec. 
```

创建分布式表

```sql
CREATE TABLE default.clusterzk_all AS cluster_zk
ENGINE = Distributed(perftest_1shards_2replicas, default, cluster_zk, rand());
```

分布式表查询数据

```shell
ode01 :) select * from clusterzk_all;

SELECT *
FROM clusterzk_all

┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

1 rows in set. Elapsed: 0.020 sec. 

```

分布式表写入

上文已经提到，`internal_replication`为`true`，则通过分布表写入数据时，会自动找到“最健康”的副本写入，然后其他副本通过表自身的复制功能同步数据，最终达到数据一致。

### 分片+数据备份整合


| ip  | 主机  | `clickhouse`  | 分片副本 |
| ------------ | ------------ | ------------ | ------------ |
| `192.168.10.100`  | `node01`  | `9000`  | `01/01`  |
| `192.168.10.100`  | `node01`  | `9001`  |  `03/02`  |
| `192.168.10.100`  | `node02`  | `9000`  | `02/01`  |
| `192.168.10.100`  | `node02`  | `9001`  |  `01/02`  |
| `192.168.10.100`  | `node03`  | `9000`  | `03/01`   |
| `192.168.10.100`  | `node03`  | `9001`  | `02/02`   |

3分片2副本.

在`node01`,`node02`,`node03`的`9001`端口再启动一个`clickhouse-server`实例。

即`shard1`的两个副本放到`node01 9000`、`node02 9001`两个机器上，`shard2`的两个副本放到`node02 9000`、`node03 9001`上，`shard3`的两个副本放到`node03 9000`、`node01 9001`上.

#### `node01`创建并修改`config1.xml`

```shell
[root@node01 clickhouse-server]# cp /etc/clickhouse-server/config.xml  /etc/clickhouse-server/config1.xml
[root@node01 clickhouse-server]# vim /etc/clickhouse-server/config1.xml
```

修改以下内容

```xml
<?xml version="1.0"?>
<yandex>
    <!--省略其他 -->
	<http_port>8124</http_port>
    <tcp_port>9001</tcp_port>
    <mysql_port>9005</mysql_port>
    <interserver_http_port>9010</interserver_http_port>
     <log>/var/log/clickhouse-server/clickhouse-server-1.log</log>
        <errorlog>/var/log/clickhouse-server/clickhouse-server.err-1.log</errorlog>
	 <!-- Path to data directory, with trailing slash. -->
    <path>/var/lib/clickhouse1/</path>
    <!-- Path to temporary data for processing hard queries. -->
    <tmp_path>/var/lib/clickhouse1/tmp/</tmp_path>
     <user_files_path>/var/lib/clickhouse1/user_files/</user_files_path>
     <format_schema_path>/var/lib/clickhouse1/format_schemas/</format_schema_path>
    <include_from>/etc/clickhouse-server/metrika1.xml</include_from>
     <!--省略其他 -->
</yandex>
```

#### `node01`创建并修改`metrika.xml`

```xml
<yandex>
<!--ck集群节点-->
<clickhouse_remote_servers>
	<!--ck集群名称-->
    <perftest_3shards_2replicas>
        <shard>
             <internal_replication>true</internal_replication>
            <replica>
                <host>node01</host>
                <port>9000</port>
            </replica>
	    <replica>
                <host>node02</host>
                <port>9001</port>
            </replica>
        </shard>
        <shard>
	   <internal_replication>true</internal_replication>
            <replica>
                <host>node02</host>
                <port>9000</port>
            </replica>
	    <replica>
                <host>node03</host>
                <port>9001</port>
            </replica>
        </shard>
        <shard>
            <internal_replication>true</internal_replication>
            <replica>
                <host>node03</host>
                <port>9000</port>
            </replica>
	    <replica>
                <host>node01</host>
                <port>9001</port>
            </replica>
        </shard>
    </perftest_3shards_2replicas>
</clickhouse_remote_servers>

<!--zookeeper相关配置-->
<zookeeper-servers>
    <node index="1">
        <host>node01</host>
        <port>2181</port>
    </node>
    <node index="2">
        <host>node02</host>
        <port>2181</port>
    </node>
    <node index="3">
        <host>node03</host>
        <port>2181</port>
    </node>
</zookeeper-servers>

<macros>
	<shard>01</shard>  <!--分ID, 同一分片内的副本配置相同的分ID-->
    <replica>node01</replica> <!--当前节点主机名-->
</macros>

<networks>
    <ip>::/0</ip>
</networks>

<!--压缩相关配置-->
<clickhouse_compression>
    <case>
        <min_part_size>10000000000</min_part_size>
        <min_part_size_ratio>0.01</min_part_size_ratio>
        <method>lz4</method> <!--压缩算法lz4压缩比zstd快, 更占磁盘-->
    </case>
</clickhouse_compression>
</yandex>

```

#### 复制`metrika.xml`文件为`metrika1.xml`，修改`macros`配置

##### `node01``metrika.xml``macros`配置

```xml
<macros>
	<shard>01</shard>  <!--分ID, 同一分片内的副本配置相同的分ID-->
    <replica>node01</replica> <!--当前节点主机名-->
</macros>
```

##### `node01``metrika1.xml``macros`配置

```xml
<macros>
	<shard>03</shard>  <!--分ID, 同一分片内的副本配置相同的分ID-->
    <replica>node01</replica> <!--当前节点主机名-->
</macros>
```

##### `node02``metrika.xml``macros`配置

```xml
<macros>
	<shard>02</shard>  <!--分ID, 同一分片内的副本配置相同的分ID-->
    <replica>node02</replica> <!--当前节点主机名-->
</macros>
```

##### `node02``metrika1.xml``macros`配置

```xml
<macros>
	<shard>01</shard>  <!--分ID, 同一分片内的副本配置相同的分ID-->
    <replica>node02</replica> <!--当前节点主机名-->
</macros>
```

##### `node03``metrika.xml``macros`配置

```xml
<macros>
	<shard>03</shard>  <!--分ID, 同一分片内的副本配置相同的分ID-->
    <replica>node03</replica> <!--当前节点主机名-->
</macros>
```

##### `node03``metrika.1xml``macros`配置

```xml
<macros>
	<shard>02</shard>  <!--分ID, 同一分片内的副本配置相同的分ID-->
    <replica>node03</replica> <!--当前节点主机名-->
</macros>
```

#### 创建并修改`clickhouse-server-1`

```shell
[root@node01 clickhouse-server]# cp /etc/rc.d/init.d/clickhouse-server  /etc/rc.d/init.d/clickhouse-server-1
You have new mail in /var/spool/mail/root
[root@node01 clickhouse-server]# vim  /etc/rc.d/init.d/clickhouse-server-1	
```

修改以下内容

```shell
CLICKHOUSE_CONFIG=$CLICKHOUSE_CONFDIR/config1.xml
CLICKHOUSE_PIDFILE="$CLICKHOUSE_PIDDIR/$PROGRAM-1.pid"
```


#### 分发配置文件到`node02`和`node03`节点

```shell
[root@node01 clickhouse-server]# scp /etc/rc.d/init.d/clickhouse-server-1 node02:/etc/rc.d/init.d/
clickhouse-server-1                                                                                                                                          100%   11KB   4.0MB/s   00:00    
You have new mail in /var/spool/mail/root
[root@node01 clickhouse-server]# scp /etc/rc.d/init.d/clickhouse-server-1 node03:/etc/rc.d/init.d/
clickhouse-server-1                                                                                                                                          100%   11KB   4.0MB/s   00:00    

[root@node01 clickhouse-server]# scp /etc/clickhouse-server/config1.xml node02:$PWD
config1.xml                                                                                                                                                  100%   33KB  10.2MB/s   00:00    
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/config1.xml node03:$PWD
config1.xml                                                                                                                                                  100%   33KB   9.7MB/s   00:00   
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/metrika.xml node02:$PWD
metrika.xml                                                                                                                                                  100% 2008     1.0MB/s   00:00    
You have new mail in /var/spool/mail/root
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/metrika.xml node03:$PWD
metrika.xml                                                                                                                                                  100% 2008     1.1MB/s   00:00    
[root@node01 clickhouse-server]# 
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/metrika1.xml node02:$PWD
metrika1.xml                                                                                                                                                 100% 2008     1.0MB/s   00:00    
[root@node01 clickhouse-server]# scp /etc/clickhouse-server/metrika1.xml node03:$PWD
metrika1.xml                  
```
**修改`node02`和`node03`的`macros`配置**

#### 启动`ClickHouse`实例

`node01`的`clickhouse-server-1`实例

`node02`的`clickhouse-server-1`实例

`node03`的`clickhouse-server`实例

`node03`的`clickhouse-server-1`实例


```shell
service clickhouse-server restart
service clickhouse-server-1 restart
```

#### 查看集群信息

```shell
node01 :) select * from system.clusters;

SELECT *
FROM system.clusters

┌─cluster───────────────────────────┬─shard_num─┬─shard_weight─┬─replica_num─┬─host_name─┬─host_address───┬─port─┬─is_local─┬─user────┬─default_database─┬─errors_count─┬─estimated_recovery_time─┐
│ perftest_3shards_2replicas        │         1 │            1 │           1 │ node01    │ 192.168.10.100 │ 9000 │        1 │ default │                  │            0 │                       0 │
│ perftest_3shards_2replicas        │         1 │            1 │           2 │ node02    │ 192.168.10.110 │ 9001 │        0 │ default │                  │            0 │                       0 │
│ perftest_3shards_2replicas        │         2 │            1 │           1 │ node02    │ 192.168.10.110 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ perftest_3shards_2replicas        │         2 │            1 │           2 │ node03    │ 192.168.10.120 │ 9001 │        0 │ default │                  │            0 │                       0 │
│ perftest_3shards_2replicas        │         3 │            1 │           1 │ node03    │ 192.168.10.120 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ perftest_3shards_2replicas        │         3 │            1 │           2 │ node01    │ 192.168.10.100 │ 9001 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         1 │            1 │           1 │ 127.0.0.1 │ 127.0.0.1      │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards           │         2 │            1 │           1 │ 127.0.0.2 │ 127.0.0.2      │ 9000 │        0 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_cluster_two_shards_localhost │         2 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost              │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_shard_localhost_secure       │         1 │            1 │           1 │ localhost │ ::1            │ 9440 │        0 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         1 │            1 │           1 │ localhost │ ::1            │ 9000 │        1 │ default │                  │            0 │                       0 │
│ test_unavailable_shard            │         2 │            1 │           1 │ localhost │ ::1            │    1 │        0 │ default │                  │            0 │                       0 │
└───────────────────────────────────┴───────────┴──────────────┴─────────────┴───────────┴────────────────┴──────┴──────────┴─────────┴──────────────────┴──────────────┴─────────────────────────┘

14 rows in set. Elapsed: 0.019 sec.
```

##### 测试分片+副本集群

创建可复制表,`node01`节点执行即可，其他节点会自动创建。

```sql
CREATE TABLE `cluster32r_local` ON cluster perftest_3shards_2replicas
	(
	`id` Int32,
    `website` String,
    `wechat` String,
	`FlightDate` Date,
	Year UInt16
)
ENGINE = ReplicatedMergeTree('/clickhouse/tables/{shard}/ontime','{replica}', FlightDate, (Year, FlightDate), 8192);
```

```shell
node01 :) CREATE TABLE `cluster32r_local` ON cluster perftest_3shards_2replicas
:-] (
:-] `id` Int32,
:-]     `website` String,
:-]     `wechat` String,
:-] `FlightDate` Date,
:-] Year UInt16
:-] )
:-] ENGINE = ReplicatedMergeTree('/clickhouse/tables/{shard}/ontime','{replica}', FlightDate, (Year, FlightDate), 8192);

CREATE TABLE cluster32r_local ON CLUSTER perftest_3shards_2replicas
(
    `id` Int32,
    `website` String,
    `wechat` String,
    `FlightDate` Date,
    `Year` UInt16
)
ENGINE = ReplicatedMergeTree('/clickhouse/tables/{shard}/ontime', '{replica}', FlightDate, (Year, FlightDate), 8192)

┌─host───┬─port─┬─status─┬─error─┬─num_hosts_remaining─┬─num_hosts_active─┐
│ node03 │ 9001 │      0 │       │                   5 │                0 │
│ node03 │ 9000 │      0 │       │                   4 │                0 │
│ node01 │ 9001 │      0 │       │                   3 │                0 │
│ node01 │ 9000 │      0 │       │                   2 │                0 │
│ node02 │ 9000 │      0 │       │                   1 │                0 │
└────────┴──────┴────────┴───────┴─────────────────────┴──────────────────┘
┌─host───┬─port─┬─status─┬─error─┬─num_hosts_remaining─┬─num_hosts_active─┐
│ node02 │ 9001 │      0 │       │                   0 │                0 │
└────────┴──────┴────────┴───────┴─────────────────────┴──────────────────┘

6 rows in set. Elapsed: 46.994 sec. 

```

创建分布式表

```sql
CREATE TABLE cluster32r_all AS cluster32r_local ENGINE = Distributed(perftest_3shards_2replicas, default, cluster32r_local, rand());
```

往第一个`shard`的副本插入数据（`node01 9000`）,可以在第二个副本中查看数据（`node02 9001`）

```sql
INSERT INTO default.cluster32r_local (id,website,wechat,FlightDate,Year)values(1,'https://niocoder.com/','java干货','2020-11-28',2020);
INSERT INTO default.cluster32r_local (id,website,wechat,FlightDate,Year)values(2,'http://www.merryyou.cn/','javaganhuo','2020-11-28',2020);
```

使用客户端链接`node02 9001`实例查看

```shell
[root@node02 ~]# clickhouse-client --port 9001 -m
ClickHouse client version 20.8.3.18.
Connecting to localhost:9001 as user default.
Connected to ClickHouse server version 20.8.3 revision 54438.

node02 :) show tables;

SHOW TABLES

┌─name─────────────┐
│ cluster32r_local │
└──────────────────┘

1 rows in set. Elapsed: 0.010 sec. 

node02 :) select * from cluster32r_local;

SELECT *
FROM cluster32r_local

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

2 rows in set. Elapsed: 0.018 sec. 
```

分布式表查询

```shell
node01 :) select * from cluster32r_all;

SELECT *
FROM cluster32r_all

┌─id─┬─website─────────────────┬─wechat─────┬─FlightDate─┬─Year─┐
│  2 │ http://www.merryyou.cn/ │ javaganhuo │ 2020-11-28 │ 2020 │
└────┴─────────────────────────┴────────────┴────────────┴──────┘
┌─id─┬─website───────────────┬─wechat───┬─FlightDate─┬─Year─┐
│  1 │ https://niocoder.com/ │ java干货 │ 2020-11-28 │ 2020 │
└────┴───────────────────────┴──────────┴────────────┴──────┘

2 rows in set. Elapsed: 0.030 sec. 
```

所有副本节点均可本地表和分布式表均可读写数据

### 下载

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/gzh.png)

> 关注微信公众号java干货回复 【clickhouse】
