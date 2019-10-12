---
layout: post
title: Spring Cloud Feign接口返回流
categories: Feign
description: Feign
keywords: Feign
---

> 身无彩凤双飞翼，心有灵犀一点通。
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java16.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java16.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java16.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java16.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java16.jpg")


### 服务提供者

```java
@GetMapping("/{id}")
    public void queryJobInfoLogDetail(@PathVariable("id") Long id, HttpServletResponse response) {

        File file = new File("xxxxx");
        InputStream fileInputStream = new FileInputStream(file);
        OutputStream outStream;
        try {
            outStream = response.getOutputStream();

            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(bytes)) != -1) {
                outStream.write(bytes, 0, len);
            }
            fileInputStream.close();
            outStream.close();
            outStream.flush();
        } catch (IOException e) {
            log.error("exception", e);
        }
    }
```

### client 客户端

```java
@GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    feign.Response queryJobInfoLogDetail(@PathVariable("id") Long id);
```

### 服务消费者

```java
    @GetMapping("/{id}")
    public void queryJobInfoLogInfoList(@PathVariable("id") Long id, HttpServletResponse servletResponse) {

        Response response = apiServices.queryJobInfoLogDetail(id);
        Response.Body body = response.body();

        InputStream fileInputStream = null;
        OutputStream outStream;
        try {
            fileInputStream = body.asInputStream();
            outStream = servletResponse.getOutputStream();

            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(bytes)) != -1) {
                outStream.write(bytes, 0, len);
            }
            fileInputStream.close();
            outStream.close();
            outStream.flush();
        } catch (Exception e) {

        }
    }
```
---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料

