---
layout: post
title: synchronized 与 volatile 的区别：门锁与公告栏的故事，面试再问也不慌
categories: Java
description: 用门锁与公告栏的故事讲透 synchronized 与 volatile 的区别：一个管互斥、一个管可见性，volatile 为什么救不了 i++，双检锁单例为什么必须加 volatile，附面试一句话答案。
keywords: synchronized, volatile, 线程安全, 可见性, 原子性, 双检锁, 单例模式, 面试
---

## 故事场景：合租公寓里的公告栏和会议室

想象你和几个室友合租一套公寓，公寓里有两个东西天天在用：

- **客厅墙上的公告栏**：谁写了新通知，所有人路过一眼就能看到最新版；
- **带锁的会议室**：一次只能进一个人，进去把门一锁，别人只能在外面等。

这就是 `volatile` 和 `synchronized` 在 Java 并发里的分工：

- **`volatile` 是公告栏**：保证你写上去的东西，所有线程立刻看得到——它管的是**可见性**；
- **`synchronized` 是门锁**：保证同一时间只有一个线程在里面办事——它管的是**互斥**，顺带也保证可见性。

一句话先记住：**只想让大家看到最新值，用 volatile；想让一段代码一次只被一个人执行，用 synchronized。**

## 第一问：没有 volatile，线程会看不见更新

先看一个经典的翻车现场。主线程想让子线程停下来，用一个普通布尔变量当开关：

```java
public class StopFlag {
    private static boolean running = true;  // 没有 volatile

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            while (running) {
                // 埋头干活
            }
            System.out.println("停下来了");
        }).start();

        Thread.sleep(1000);
        running = false;  // 主线程喊停
        System.out.println("已经喊停了");
    }
}
```

这段代码很可能永远停不下来——"已经喊停了"打印了，子线程还在转圈。

问题出在**缓存**：每个线程为了跑得快，会把变量抄一份放在自己的"小抄本"（CPU 缓存、工作内存）里。主线程把 `running` 改成 `false`，改的是主内存里的正本；子线程手里还攥着旧的小抄，不知道正本已经变了。

给变量加上 `volatile`，就相当于把它贴到了公告栏上：

```java
private static volatile boolean running = true;
```

`volatile` 的规矩只有一条，但很硬：**每次读都直接去主内存读，每次写都立刻写回主内存，不许抄小抄。** 这样主线程一改，所有线程下次读到的就是新值，停机命令再也不会被"视而不见"。

面试官常追问："volatile 是怎么做到这一点的？"标准答案是：它在读写前后插入了**内存屏障**，禁止指令重排序、强制刷新缓存。记住"内存屏障"这四个字，面试就够用了。

## 第二问：volatile 为什么救不了 i++

既然 volatile 这么好用，那计数器是不是加个 volatile 就线程安全了？不是。这是面试里最常见的坑：

```java
public class Counter {
    private volatile int count = 0;

    public void add() {
        count++;  // 看似一行，实际是三步
    }
}
```

`count++` 看着是一行代码，到了底层是三个动作：**读出旧值、加一、写回新值**。volatile 只保证你读到的是最新值、写回去立刻生效，但它管不了"读和写之间有没有人插队"。

两个线程同时读到 `count = 5`，各自加一，都写回 `6`——一次加一就这么丢了。三个线程、十个线程一起上，丢得更多。

这就是 volatile 的边界：**它保证可见性，不保证原子性。** 只要操作是"先读后写"的复合动作，volatile 就兜不住。这类活儿得交给门锁：

```java
public class Counter {
    private int count = 0;

    public synchronized void add() {
        count++;  // 同一时间只有一个线程能执行这三步
    }
}
```

`synchronized` 把"读—加—写"三步锁进会议室，一次只放一个人进去，别人只能在门口排队，插队自然就没了。

一句话区分：**单个变量的读写，用 volatile；读改写一体的复合操作，用 synchronized（或者 AtomicInteger 这类原子类）。**

## 第三问：synchronized 除了互斥，还附赠了什么

很多人只记得 synchronized 是把锁，其实它进门的时候还顺手干了两件事：

1. **可见性**：线程进门（获得锁）时会把缓存里的小抄作废，强制从主内存重读；出门（释放锁）时会把改过的值写回主内存。所以被 synchronized 保护的变量，天然就是"人人可见"的。
2. **有序性**：锁里头的代码不会被重排到锁外面去，锁的边界就是一道屏障。

```java
public class TicketWindow {
    private int tickets = 100;

    public synchronized boolean sellOne() {
        if (tickets > 0) {
            tickets--;
            return true;   // 读、判断、减一，三步都在锁里完成
        }
        return false;
    }
}
```

查票、减票这两个动作必须绑在一起做，中间不许插队——这正是"售票窗口一次只服务一个人"的日常道理。锁在这里同时保证了三件事：**没人插队（互斥）、所有人看到的余票数都是最新的（可见性）、查和减的顺序不会乱（有序性）**。

顺带提一句性能：JDK 6 之后 synchronized 不再是动不动就"上重量级锁"的傻大个，它会按竞争程度从偏向锁、轻量级锁一路升级到重量级锁，没人抢的时候开销很小。面试问"synchronized 会不会很慢"，就答这一句：**现代 JVM 里无竞争的 synchronized 很便宜，别再拿老黄历说它慢了。**

## 第四问：双检锁单例——两个关键字必须联手

这是 volatile 和 synchronized 唯一一次"必须同时出场"的名场面，面试官最爱考：

```java
public class Singleton {
    private static volatile Singleton instance;  // volatile 不能省

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {                    // 第一次检查：没锁，快
            synchronized (Singleton.class) {       // 加锁
                if (instance == null) {            // 第二次检查：锁里再确认
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

为什么 `instance` 前面的 `volatile` 不能省？因为 `new Singleton()` 不是一个原子动作，它分三步：**分配内存、初始化对象、把引用指向这块内存**。编译器和 CPU 可能把后两步调换顺序——引用先指向了内存，对象还没初始化完。

没有 volatile，线程 A 刚把引用指向一块"还没装修完的房子"，线程 B 第一次检查发现 `instance` 不为 null，直接拿去用了，结果拿到一个半成品。

`volatile` 在这里的职责是**禁止重排序**：保证"初始化完成"一定发生在"引用可见"之前。这也是 volatile 的第三个作用：**可见性、原子性不管、但禁止指令重排序它管。**

记住这个结论：**双检锁单例里，volatile 防的是"半成品对象被提前看到"，synchronized 防的是"两个线程各造一个单例"。**

## 一张表记住今天的内容

|  | volatile（公告栏） | synchronized（门锁） |
|---|---|---|
| 管什么 | 可见性 + 禁止重排序 | 互斥 + 可见性 + 有序性 |
| 保证原子性 | 不保证 | 保证（锁住的代码块整体原子） |
| 适用场景 | 状态标志位、单次读写 | 复合操作、临界区 |
| 会不会阻塞 | 不会，读写直接走主内存 | 会，抢不到锁的线程要等 |
| 典型用法 | `volatile boolean running`、`volatile Singleton instance` | 计数器自增、售票、双检锁里的同步块 |

## 文末留一句话

下次面试官问"synchronized 和 volatile 有什么区别"，别从定义开始背，先讲这间合租公寓：公告栏保证人人看到最新通知，会议室保证一次只进一个人办事。**只想让值人人可见，贴公告栏；想让一段操作不被插队，上门锁；** 至于 `i++` 为什么 volatile 救不了、双检锁为什么两个关键字缺一不可，顺着这个故事讲下去，答案自己就出来了。

---

和这篇搭着看：线程为什么会看不见别的线程改过的值，根子在 JVM 的内存模型上，见那篇[《JVM 内存结构：一栋宿舍楼的分房故事，面试再问也不慌》](/2026/11/09/JVM内存结构/)，这篇讲"谁住哪间房"、那篇讲"房与房之间怎么同步"，正好一对；想看看多线程在真实项目里怎么排兵布阵，见我早年写的[《线程池核心参数》](/2026/10/25/线程池核心参数/)，锁和队列搭着用才是日常。

> 动手验证：把第一问那段停不下来的代码跑一遍，亲眼看看子线程是怎么"装聋"的，再给 `running` 加上 `volatile` 重跑一次，对比两次结果——可见性这回事，跑一次就记住了；再把第二问的计数器开 10 个线程各加 1000 次，数数最后离 10000 差多少，就知道 volatile 为什么救不了 `i++`。想在云上练手可以看我整理的[云服务器选购指南](https://go.52upload.cn/articles/vps-guide.html)，新客首单很便宜，别买贵了；算时间、转格式这类小活儿，我的在线工具箱 [itupload.cn](https://itupload.cn) 打开就能用。
