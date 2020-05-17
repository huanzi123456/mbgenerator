###推荐阅读(官网):
    https://mybatis.plus/guide/generator.html

###架构介绍:

###内容使用jar与不常见的类分析 
 + Configuration  PropertiesConfiguration.class 
 + ResultSet =  DatabaseMetaData (jdbc)
 + ZipOutputStream (jdk.util)
 
 + Velocity 
 + VelocityContext
 + Template
 
```$xslt
//流程可以查看代码注释!
主要是利用jdbc的一些sql,完成对数据库,表信息的获取!
然后通过获取的信息,模板信息与io流写出生成code!
```
###相关知识
```$xslt
1.jdbc知识点:

2.相关mysql的数据库,表信息语句基础知识

3.io流知识


```