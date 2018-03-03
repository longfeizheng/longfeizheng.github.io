---
layout: post
title: JPA关系映射系列四：many-to-many 关联映射
categories: JPA
description: JPA
keywords: JPA
---
> SpringDataJPA是Spring Data的一个子项目，通过提供基于JPA的Repository极大的减少了JPA作为数据访问方案的代码量，你仅仅需要编写一个接口集成下SpringDataJPA内部定义的接口即可完成简单的CRUD操作。

## 前言
本篇文章引导你通过`Spring Boot`，`Spring Data JPA`和`MySQL`实现`many-to-many`关联映射。

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
#### 多对多关联映射

##### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa08.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa08.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa08.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa08.png")

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

    <artifactId>many-to-many</artifactId>

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
##### 多对多关联
`book.id` 和 `publisher.id` 多对多关联表`book_publisher`
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa09.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa09.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa09.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa09.png")

db.sql

```sql
CREATE DATABASE  IF NOT EXISTS `jpa_manytomany`;
USE `jpa_manytomany`;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`name` varchar(255) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

--
-- Table structure for table `publisher`
--

DROP TABLE IF EXISTS `publisher`;
CREATE TABLE `publisher` (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`name` varchar(255) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;
--
-- Table structure for table `book_publisher`
--

DROP TABLE IF EXISTS `book_publisher`;
CREATE TABLE `book_publisher` (
`book_id` int(10) unsigned NOT NULL,
`publisher_id` int(10) unsigned NOT NULL,
PRIMARY KEY (`book_id`,`publisher_id`),
KEY `fk_bookpublisher_publisher_idx` (`publisher_id`),
CONSTRAINT `fk_bookpublisher_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `fk_bookpublisher_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `publisher` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


```
##### 实体类
###### Book

```java
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "book_publisher", joinColumns = @JoinColumn(name = "book_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "publisher_id", referencedColumnName = "id"))
    private Set<Publisher> publishers = new HashSet<>();

    public Book() {

    }

    public Book(String name) {
        this.name = name;
    }

    public Book(String name, Set<Publisher> publishers) {
        this.name = name;
        this.publishers = publishers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Publisher> getPublishers() {
        return publishers;
    }

    public void setPublishers(Set<Publisher> publishers) {
        this.publishers = publishers;
    }

    @Override
    public String toString() {
        String result = String.format(
                "Book [id=%d, name='%s']%n",
                id, name);
        if (publishers != null) {
            for (Publisher publisher : publishers) {
                result += String.format(
                        "Publisher[id=%d, name='%s']%n",
                        publisher.getId(), publisher.getName());
            }
        }

        return result;
    }
}

```
###### Publisher
```java
@Entity
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    @ManyToMany(mappedBy = "publishers")
    private Set<Book> books = new HashSet<>();

    public Publisher(){

    }

    public Publisher(String name){
        this.name = name;
    }

    public Publisher(String name, Set<Book> books){
        this.name = name;
        this.books = books;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        String result = String.format(
                "Publisher [id=%d, name='%s']%n",
                id, name);
        if (books != null) {
            for(Book book : books) {
                result += String.format(
                        "Book[id=%d, name='%s']%n",
                        book.getId(), book.getName());
            }
        }

        return result;
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
	
- `@Column` 声明该属性与数据库字段的映射关系。
- `@ManyToMany` 多对多关联关系
- `@JoinColumn` 指定关联的字段

##### Spring Data JPA Repository
###### BookRepository
```java
public interface BookRepository extends JpaRepository<Book, Integer> {
}
```

###### PublisherRepository
```java
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
}
```

`Spring Data JPA`包含了一些内置的`Repository`，实现了一些常用的方法：`findone`，`findall`，`save`等。

##### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/jpa_manytomany
    username: root
    password: admin
    driver-class-name: com.mysql.jdbc.Driver
  jpa:
    show-sql: true
    properties:
      hibernate:
        enable_lazy_load_no_trans: true
```
##### BookRepositoryTest

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Test
    public void saveTest() throws Exception {

        Publisher publisherA = new Publisher("Publisher One");
        Publisher publisherB = new Publisher("Publisher Two");

        Book bookA = new Book("Book One");
        bookA.setPublishers(new HashSet<Publisher>() {{
            add(publisherA);
            add(publisherB);
        }});

        bookRepository.save(bookA);

    }

    @Test
    public void saveTest1() throws Exception{
        Publisher publisher = publisherRepository.findOne(24);
        Book bookA = new Book("Book Two");
        bookA.getPublishers().add(publisher);
        bookRepository.save(bookA);
    }

    @Test
    public void saveTest2() throws Exception{
        Book two = bookRepository.findOne(18);
        Publisher publisher = publisherRepository.findOne(25);
        two.getPublishers().add(publisher);
        bookRepository.save(two);
    }

    @Test
    public void findPublisherTest() throws Exception{
        Publisher publisher = publisherRepository.findOne(24);
        Set<Book> books = publisher.getBooks();
        for(Book book: books){
            log.info(book.getName()+"..."+book.getId());
        }
        Assert.assertNotNull(publisher);
        Assert.assertNotNull(publisher.getName());
    }

    @Test
    public void findAllTest() throws Exception {
        for (Book book : bookRepository.findAll()) {
            log.info(book.toString());
        }
    }

    @Test
    public void findBookTest() throws Exception{
        Book book = bookRepository.findOne(16);
        Set<Publisher> publishers = book.getPublishers();
        for(Publisher publisher: publishers){
            log.info(publisher.getName()+"..."+publisher.getId());
        }
        Assert.assertNotNull(book);
        Assert.assertNotNull(book.getName());
    }
}
```

## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/jpa-example/tree/master/many-to-many](https://github.com/longfeizheng/jpa-example/tree/master/many-to-many)

---
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？