---
layout: post
title: RabbitMQ 系列一：Hello World
categories: RabbitMQ
description: RabbitMQ
keywords: RabbitMQ
---

>生活不止眼前的苟且，还有永远读不懂的诗和到不了的远方。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.jpg")

## 概述 ##

RabbitMQ是实现了高级消息队列协议（AMQP）的开源消息代理软件（亦称面向消息的中间件）。RabbitMQ服务器是用Erlang语言编写的，而集群和故障转移是构建在开放电信平台框架上的。所有主要的编程语言均有与代理接口通讯的客户端库。

### 安装RabbitMQ

 `RabbitMQ`下载[地址](https://www.rabbitmq.com/download.html),本系列我们采用`docker`的方式来安装`RabbitMQ`，`RabbitMQ`的`docker`镜像[地址](https://hub.docker.com/_/rabbitmq/)。关于如何`docker`的安装和使用可参考以下[链接](https://niocoder.com/categories/#Docker);

启动`RabbitMQ`

`docker run -d -p 15672:15672  -p  5672:5672  -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin --name rabbitmq --hostname=rabbitmqhostone  daocloud.io/library/rabbitmq:3.7.14-management-alpine`

安装完成之后可通过`http://localhost:15672/#/`访问图形界面(用户名密码均为`admin`)

### RabbitMQ 架构图如下

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.png")

1. `Server` 简单来说就是消息队列服务器实体
2. `Exchange` 消息交换机，它指定消息按什么规则，路由到哪个队列
3. `Queue` 消息队列载体，每个消息都会被投入到一个或多个队列
4. `Binding`： 绑定，它的作用就是把`exchange`和`queue`按照路由规则绑定起来
5. `Routing Key`： 路由关键字，`exchange`根据这个关键字进行消息投递
6. `VHost`： 虚拟主机，一个`broker`里可以开设多个`vhost`，用作不同用户的权限分离。
7. `Producer`： 消息生产者，就是投递消息的程序
8. `Consumer`： 消息消费者，就是接受消息的程序
9. `Channel`： 消息通道，在客户端的每个连接里，可建立多个`channel`，每个`channel`代表一个会话任务

注意：由`Exchange`、`Queue`、`RoutingKey`三个才能决定一个从`Exchange`到`Queue`的唯一的线路。

### RabbitMQ 消息模型

关于`RabbitMQ` [消息模型](https://www.rabbitmq.com/getstarted.html)

- `Direct`交换机：完全根据`key`进行投递。
- `Topic`交换机：在`key`进行模式匹配后进行投递。
- `Fanout`交换机：它采取广播模式，消息进来时，将会被投递到与改交换机绑定的所有队列中。

### RabbitMQ Hello World

#### 生产者 Procuder

```java
package com.niocoder.quickstart;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Procuder {

    public static void main(String[] args) throws IOException, TimeoutException {

        // 1. 创建ConnectionFactory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");

        // 2. 通过链接工厂创建连接
        Connection connection = factory.newConnection();

        // 3. 通过connection 创建一个channel
        Channel channel = connection.createChannel();

        // 4. 通过channel发送数据
        for (int i = 0; i < 3; i++) {
            String msg = "Hello World";
            /**
             *      * @param exchange the exchange to publish the message to
             *      * @param routingKey the routing key
             *      * @param props other properties for the message - routing headers etc
             *      * @param body the message body
             */
            channel.basicPublish("", "hello", null, msg.getBytes());
        }

        // 5. 关闭连接
        channel.close();
        connection.close();
    }
}

```

#### 消费者 Consumer


```java
package com.niocoder.quickstart;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class Consumer {
    public static void main(String[] args) throws Exception {
        // 1. 创建ConnectionFactory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");

        // 2. 通过链接工厂创建连接
        Connection connection = factory.newConnection();

        // 3. 通过connection 创建一个channel
        Channel channel = connection.createChannel();

        // 4. 创建一个队列
        String queueName = "hello";
        /**
         *      * @param queue the name of the queue
         *      * @param durable true if we are declaring a durable queue (the queue will survive a server restart)
         *      * @param exclusive true if we are declaring an exclusive queue (restricted to this connection)
         *      * @param autoDelete true if we are declaring an autodelete queue (server will delete it when no longer in use)
         *      * @param arguments other properties (construction arguments) for the queue
         */
        channel.queueDeclare(queueName, true, false, false, null);

        // 5. 创建消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);

        // 6. 设置channel
        channel.basicConsume(queueName,true,consumer);

        while (true){
            // 7. 获取消息
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String msg = new String(delivery.getBody());
            System.err.println("消费端: " + msg);
        }
    }
}

```

#### 效果如下：

先启动消费者，再启动生产者

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/mq/mq01.gif")

## 代码下载

- [https://github.com/longfeizheng/rabbitmq-tutorials](https://github.com/longfeizheng/rabbitmq-tutorials)
