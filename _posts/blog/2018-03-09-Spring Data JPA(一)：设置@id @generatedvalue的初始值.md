---
layout: post
title: Spring Data JPA(一)：@id @generatedvalue设置初始值
categories: JPA
description: JPA
keywords: JPA
---
> SpringDataJPA是Spring Data的一个子项目，通过提供基于JPA的Repository极大的减少了JPA作为数据访问方案的代码量，你仅仅需要编写一个接口集成下SpringDataJPA内部定义的接口即可完成简单的CRUD操作。

## 前言
本篇文章引导你通过`Spring Boot`，`Spring Data JPA`和`MySQL`实现设置`@id` `@generatedvalue`初始值从10000自增。

### 准备
- JDK 1.8 或更高版本
- Maven 3 或更高版本
- MySQL Server 5.6 

### 技术栈
- Spring Data JPA
- Spring Boot
- MySQL

### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa12.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa12.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/Spring-data-jpa12.png")
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
        <module>initial-value-generator</module>
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
#### @id @generatedvalue设置初始值

##### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa13.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa13.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/jpa/spring-data-jpa13.png")

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

    <artifactId>initial-value-generator</artifactId>

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
##### db.sql

```sql
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` bigint(20) NOT NULL,
  `city` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `zip` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of address
-- ----------------------------

-- ----------------------------
-- Table structure for app_seq_store
-- ----------------------------
DROP TABLE IF EXISTS `app_seq_store`;
CREATE TABLE `app_seq_store` (
  `APP_SEQ_NAME` varchar(255) NOT NULL,
  `APP_SEQ_VALUE` int(11) NOT NULL,
  PRIMARY KEY (`APP_SEQ_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of app_seq_store
-- ----------------------------
INSERT INTO `app_seq_store` VALUES ('LISTENER_PK', '10000');

```
##### 实体类
###### Address

```java
@Entity
public class Address {
    @Id
    @Column( name = "ID" )
    @TableGenerator(
            name = "AppSeqStore",
            table = "APP_SEQ_STORE",
            pkColumnName = "APP_SEQ_NAME",
            pkColumnValue = "LISTENER_PK",
            valueColumnName = "APP_SEQ_VALUE",
            initialValue = 10000,
            allocationSize = 1 )
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "AppSeqStore" )
    private long id;

    private String street;

    private String city;

    private String state;

    private String zip;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String address) {
        this.street = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String toString() {
        return "Address id: " + getId() + ", street: " + getStreet() + ", city: " + getCity()
                + ", state: " + getState() + ", zip: " + getZip();
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
- `name` 声明该表主键生成策略的名称，它被引用在@GeneratedValue中设置的“generator”值中;
- `table` 声明表生成策略所持久化的表名;
- `pkColumnName` 声明在持久化表中，该主键生成策略所对应键值的名称;
- `valueColumnName` 声明在持久化表中，该主键当前所生成的值，它的值将会随着每次创建累加;
- `pkColumnValue` 声明在持久化表中，该生成策略所对应的主键。
- `initialValue` 声明主键初识值，默认为0
- `allocationSize` 声明每次主键值增加的大小


##### Spring Data JPA Repository
###### AddressRepository
```java
public interface AddressRepository extends JpaRepository<Address, Integer> {
}
```

`Spring Data JPA`包含了一些内置的`Repository`，实现了一些常用的方法：`findone`，`findall`，`save`等。

##### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/initial-value-generator
    username: root
    password: admin
    driver-class-name: com.mysql.jdbc.Driver
  jpa:
    show-sql: true
    properties:
      hibernate:
        enable_lazy_load_no_trans: true
#    hibernate:
#      ddl-auto: create
```
##### AddressRepositoryTest

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Test
    public void testSave(){
        Address address = new Address();
        address.setCity("beijing");
        address.setState("02");
        address.setStreet("street");
        address.setZip("aa.zip");
        Address result = addressRepository.save(address);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getId());
        Assert.assertTrue(result.getId()>=10000);
    }
}
```

## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/jpa-example/tree/master/initial-value-generator](https://github.com/longfeizheng/jpa-example/tree/master/initial-value-generator)

---
> [JPA关系映射系列一：one-to-one外键关联](http://blog.csdn.net/xinan8801/article/details/60963535)
[JPA关系映射系列二：one-to-one主键关联](https://longfeizheng.github.io/2018/03/01/JPA%E5%85%B3%E7%B3%BB%E6%98%A0%E5%B0%84%E7%B3%BB%E5%88%97%E4%BA%8C-one-to-one%E4%B8%BB%E9%94%AE%E5%85%B3%E8%81%94/)
[JPA关系映射系列三：one-to-many和many-to-one](https://longfeizheng.github.io/2018/03/01/JPA%E5%85%B3%E7%B3%BB%E6%98%A0%E5%B0%84%E7%B3%BB%E5%88%97%E4%B8%89-one-to-many%E5%92%8Cmany-to-one/)
[JPA关系映射系列四：many-to-many 关联映射](https://longfeizheng.github.io/2018/03/03/JPA%E5%85%B3%E7%B3%BB%E6%98%A0%E5%B0%84%E7%B3%BB%E5%88%97%E5%9B%9B-many-to-many%E5%85%B3%E9%94%AE%E6%98%A0%E5%B0%84/)
[JPA关系映射系列五：many-to-many 关联表存在额外字段关系映射](https://longfeizheng.github.io/2018/03/06/JPA%E5%85%B3%E7%B3%BB%E6%98%A0%E5%B0%84%E7%B3%BB%E5%88%97%E4%BA%94-many-to-many-%E5%AD%98%E5%9C%A8%E9%A2%9D%E5%A4%96%E5%AD%97%E6%AE%B5%E5%85%B3%E8%81%94%E6%98%A0%E5%B0%84/)

---
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？