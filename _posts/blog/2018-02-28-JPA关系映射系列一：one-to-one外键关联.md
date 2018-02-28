---
layout: post
title: JPA关系映射系列一：one-to-one外键关联
categories: JPA
description: JPA
keywords: JPA
---
> SpringDataJPA是Spring Data的一个子项目，通过提供基于JPA的Repository极大的减少了JPA作为数据访问方案的代码量，你仅仅需要编写一个接口集成下SpringDataJPA内部定义的接口即可完成简单的CRUD操作。

## 前言
本篇文章引导你通过`Spring Boot`，`Spring Data JPA`和`MySQL`实现一对一外键关联映射。

### 准备
- JDK 1.8 或更高版本
- Maven 3 或更高版本
- MySQL Server 5.6 

### 技术栈
- Spring Data JPA
- Spring Boot
- MySQL

### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa01.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa01.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa01.png")
### 父pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>cn.merryyou</groupId>
    <artifactId>jpa-example</artifactId>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>one-to-one-foreignkey</module>
        <module>one-to-one-primarykey</module>
        <module>one-to-many</module>
        <module>many-to-many</module>
        <module>many-to-many-extra-columns</module>
    </modules>
    <packaging>pom</packaging>


    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.spring.platform</groupId>
                <artifactId>platform-bom</artifactId>
                <version>Brussels-SR6</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```
#### 一对一外键

##### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa02.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa02.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa02.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa02.png")

##### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>jpa-example</artifactId>
        <groupId>cn.merryyou</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>one-to-one-foreignkey</artifactId>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.6.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
##### 一对一关系
`book.book_detail_id` 和 `book_detail.id`
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa03.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa03.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa03.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa03.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa03.png")

db.sql

```sql
CREATE DATABASE  IF NOT EXISTS `jpa_onetoone_foreignkey`;
USE `jpa_onetoone_foreignkey`;

--
-- Table structure for table `book_detail`
--

DROP TABLE IF EXISTS `book_detail`;
CREATE TABLE `book_detail` (
`id` int(11) unsigned NOT NULL AUTO_INCREMENT,
`number_of_pages` int(11) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
`id` int(11) unsigned NOT NULL AUTO_INCREMENT,
`name` varchar(255) DEFAULT NULL,
`book_detail_id` int(11) unsigned DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `fk_book_bookdetail` (`book_detail_id`),
CONSTRAINT `fk_book_bookdetail` FOREIGN KEY (`book_detail_id`) REFERENCES `book_detail` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
```
##### 实体类
###### Book

```java
@Entity
@Data
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "book_detail_id")
//    @Lazy(false)
    private BookDetail bookDetail;

    public Book() {
    }

    public Book(String name, BookDetail bookDetail) {
        this.name = name;
        this.bookDetail = bookDetail;
    }
}
```
###### BookDetail
```java
@Entity
@Table(name = "book_detail")
@Data
public class BookDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    @OneToOne(mappedBy = "bookDetail")
    private Book book;

    public BookDetail() {
    }

    public BookDetail(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
}

```
- `@Table`声明此对象映射到数据库的数据表，通过它可以为实体指定表(talbe),目录(Catalog)和schema的名字。该注释不是必须的，如果没有则系统使用默认值(实体的短类名)。
- `@Id` 声明此属性为主键。该属性值可以通过应该自身创建，但是Hibernate推荐通过Hibernate生成
- `@GeneratedValue` 指定主键的生成策略。
	1. TABLE：使用表保存id值
	2. IDENTITY：identitycolumn
	3. SEQUENCR ：sequence
	4. AUTO：根据数据库的不同使用上面三个
	
- ` @Column` 声明该属性与数据库字段的映射关系。
- `@OneToOne` 一对一关联关系
- `@JoinColumn` 指定关联的字段

##### Spring Data JPA Repository
```java
public interface BookRepository extends JpaRepository<Book, Integer> {
}
```

`Spring Data JPA`包含了一些内置的`Repository`，实现了一些常用的方法：`findone`，`findall`，`save`等。

##### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/jpa_onetoone_foreignkey
    username: root
    password: admin
    driver-class-name: com.mysql.jdbc.Driver
  jpa:
    show-sql: true
```
##### BookRepositoryTest

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void saveTest() throws Exception {

        List<Book> books = new ArrayList<>();
        books.add(new Book("Book one", new BookDetail(1)));
        books.add(new Book("Book two", new BookDetail(2)));
        books.add(new Book("Book three", new BookDetail(3)));
        List<Book> bookList = bookRepository.save(books);
        Assert.assertNotNull(bookList);
        Assert.assertEquals(3, bookList.size());
    }

    @Test
    public void findBooksTest() throws Exception{
        List<Book> books = bookRepository.findAll();
        for (Book book: books) {
            log.info(book.toString());
        }
    }

}
```

## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/jpa-example/tree/master/one-to-one-foreignkey](https://github.com/longfeizheng/jpa-example/tree/master/one-to-one-foreignkey)

---
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？