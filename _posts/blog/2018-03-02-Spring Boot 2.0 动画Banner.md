---
layout: post
title: Spring Boot 2.0 动画Banner
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---
> `Spring Boot`是由Pivotal团队提供的全新框架，其设计目的是用来简化新Spring应用的初始搭建以及开发过程。[v2.0.0.RELEASE](https://github.com/spring-projects/spring-boot/releases/tag/v2.0.0.RELEASE)已于昨天正式发布。

## 前言
本篇文章引导你通过`Spring Boot`，`Spring Data JPA`和`MySQL`实现`one-to-many`和`many-to-one`关联映射。

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
#### 一对多和多对一

##### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa06.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa06.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa06.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa06.png")

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

    <artifactId>one-to-many</artifactId>

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
##### 一对多和多对一关联
`book.book-category_id` 和 `book_category.id`
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa07.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa07.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa07.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa07.png")

db.sql

```sql
CREATE DATABASE  IF NOT EXISTS `jpa_onetomany`;
USE `jpa_onetomany`;

--
-- Table structure for table `book_detail`
--

DROP TABLE IF EXISTS `book_category`;
CREATE TABLE `book_category` (
`id` int(11) unsigned NOT NULL AUTO_INCREMENT,
`name` varchar(255) NOT NULL,
PRIMARY KEY (`id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
`id` int(11) unsigned NOT NULL AUTO_INCREMENT,
`name` varchar(255) DEFAULT NULL,
`book_category_id` int(11) unsigned DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `fk_book_bookcategoryid_idx` (`book_category_id`),
CONSTRAINT `fk_book_bookcategoryid` FOREIGN KEY (`book_category_id`) REFERENCES `book_category` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
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

    @ManyToOne
    @JoinColumn(name = "book_category_id")
    private BookCategory bookCategory;

    public Book() {

    }

    public Book(String name) {
        this.name = name;
    }

    public Book(String name, BookCategory bookCategory) {
        this.name = name;
        this.bookCategory = bookCategory;
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

    public BookCategory getBookCategory() {
        return bookCategory;
    }

    public void setBookCategory(BookCategory bookCategory) {
        this.bookCategory = bookCategory;
    }
}
```
###### BookCategory
```java
@Entity
@Table(name = "book_category")
public class BookCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;

    @OneToMany(mappedBy = "bookCategory", cascade = CascadeType.ALL)
    private Set<Book> books;

    public BookCategory(){

    }

    public BookCategory(String name) {
        this.name = name;
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
                "Category[id=%d, name='%s']%n",
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
- `@OneToMany` 一对多关联关系
- `@ManyToOne` 多对一关联关系
- `@JoinColumn` 指定关联的字段

##### Spring Data JPA Repository
```java
public interface BookCategoryRepository extends JpaRepository<BookCategory, Integer> {
}
```

`Spring Data JPA`包含了一些内置的`Repository`，实现了一些常用的方法：`findone`，`findall`，`save`等。

##### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/jpa_onetomany
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
public class BookCategoryRepositoryTest {

    @Autowired
    private BookCategoryRepository repository;


    @Test
    public void saveCategoryTest(){
        BookCategory categoryOne = new BookCategory("Category One");
        Set books = new HashSet<Book>(){{
            add(new Book("Book One", categoryOne));
            add(new Book("Book Two", categoryOne));
            add(new Book("Book Three", categoryOne));
        }};
        categoryOne.setBooks(books);

        BookCategory categoryTwo = new BookCategory("Category Two");
        Set bookBs = new HashSet<Book>(){{
            add(new Book("Book Four", categoryTwo));
            add(new Book("Book Five", categoryTwo));
            add(new Book("Book Six", categoryTwo));
        }};
        categoryTwo.setBooks(bookBs);

        Set allBooks = new HashSet();

        allBooks.add(categoryOne);
        allBooks.add(categoryTwo);

        List list = repository.save(allBooks);

        Assert.assertNotNull(list);
    }

    @Test
    public void findAll() throws Exception{

        for (BookCategory bookCategory : repository.findAll()) {
            log.info(bookCategory.toString());
        }
    }
}
```

## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/jpa-example/tree/master/one-to-many](https://github.com/longfeizheng/jpa-example/tree/master/one-to-many)

---
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？