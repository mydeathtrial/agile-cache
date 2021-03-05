# agile-cache ： 缓存组件

[![](https://img.shields.io/badge/Spring-LATEST-green)](https://img.shields.io/badge/spring-LATEST-green)
[![](https://img.shields.io/badge/build-maven-green)](https://img.shields.io/badge/build-maven-green)

## 它有什么作用

* **缓存切换**
  切换方式与spring boot cache切换方式一致，均使用元数据spring.cache.type进行配置，当不存在该配置情况下，默认使用内存介质

* **统一操作方式**
  解析器通过提供CacheUtil、AgileCache，屏蔽掉各类型缓存介质的操作差异，以最简单的形式提供开发者开箱即用的缓存操作

* **缓存过期**
  支持存储过程中直接设置缓存过期时间

* **分布式/集群锁**
  当缓存介质为redis时，通过CacheUtil或AgileCache的lock与unlock提供锁操作

* **集合数据操作**
  CacheUtil、AgileCache针对不同存储介质提供一致性的集合数据操作API，参考快速入门

* **支持Hibernate二级缓存**
  提供EhCache与Redis作为Hibernate二级缓存介质，并提供元数据形式的EhCache缓存配置方式。并且确保spring与hibernate二级缓存共享缓存管理器CacheManager

* **支持缓存介质扩展**
  默认提供内存、EhCache、Redis三种缓存介质，开发人员可以根据实际需求，自行实现抽象类AgileCacheManager（缓存管理器抽象类）与AbstractAgileCache（缓存抽象类）并注入到spring容器中
  实现方式非常简单

-------

## 快速入门

开始你的第一个项目是非常容易的。

#### 步骤 1: 下载包

您可以从[最新稳定版本]下载包(https://github.com/mydeathtrial/agile-cache/releases). 该包已上传至maven中央仓库，可在pom中直接声明引用

以版本agile-cache-2.0.10.jar为例。

#### 步骤 2: 添加maven依赖

```xml
<!--声明中央仓库-->
<repositories>
    <repository>
        <id>cent</id>
        <url>https://repo1.maven.org/maven2/</url>
    </repository>
</repositories>
        <!--声明依赖-->
<dependency>
<groupId>cloud.agileframework</groupId>
<artifactId>agile-cache</artifactId>
<version>2.0.10</version>
</dependency>
```

#### 步骤 3: 程序中调用CacheUtil（例）

```java
public class YourClass {
    public void test() {
        /**
         * 添加缓存
         * @param key   缓存索引值，Object对象，一般使用字符串
         * @param value 缓存数据，Object对象，支持任意形式参数，当缓存介质为redis时，该对象需要实现序列化接口，以便存取过程中的正反序列化，redis默认使用JDK方式序列化该值
         **/
        CacheUtil.put("key", "value");

        /**
         * 如果不存在就存，存在就不存
         * @param key   缓存索引值，Object对象，一般使用字符串，缓存索引值
         * @param value 缓存数据，Object对象，支持任意形式参数，当缓存介质为redis时，该对象需要实现序列化接口，以便存取过程中的正反序列化，redis默认使用JDK方式序列化该值
         **/
        CacheUtil.putIfAbsent("key", "value");

        /**
         * 如果不存在就存，存在就不存
         * @param key     缓存索引值，Object对象，一般使用字符串，缓存索引值
         * @param value   缓存数据，Object对象，支持任意形式参数，当缓存介质为redis时，该对象需要实现序列化接口，以便存取过程中的正反序列化，redis默认使用JDK方式序列化该值
         * @param timeout 缓存过期时长，Duration对象，从存放时间点开始计算，过期后自动与缓存中清除该缓存数据
         **/
        CacheUtil.putIfAbsent("key", "value", Duration.ofHours(1));

        /**
         * 删除缓存
         * @param key   缓存索引值，Object对象，一般使用字符串，缓存索引值
         **/
        CacheUtil.evict("key");

        /**
         * 清空公共区域缓存
         **/
        CacheUtil.clear();

        /**
         * 判断缓存是否存在，true存在，false不存在
         * @param key   缓存索引值，Object对象，一般使用字符串，缓存索引值
         **/
        boolean isHave = CacheUtil.containKey("key");

        /**
         * 取缓存
         * @param key   缓存索引值，Object对象，一般使用字符串，缓存索引值
         * @param value 缓存数据类型，Class对象，用于取值后的反序列化过程，该值支持复杂数据类型
         **/
        CacheUtil.get("key", Integer.class);

        /**
         * 向Map中添加数据，方法调用前需要确保缓存中已经存放过key值为mapKey，value为Map结果的数据。
         *
         * @param mapKey 缓存索引，缓存解析器会根据mapKey于缓存中查找对应的Map结构缓存，取出后操作存储
         * @param key    map结构中的key无类型限制
         * @param value  map结构中的value无类型限制
         */
        CacheUtil.addToMap("mapKey", "key", "value");

        /**
         * 取缓存
         * @param mapKey 缓存索引，缓存解析器会根据mapKey于缓存中查找对应的Map结构缓存，取出后操作取值
         * @param key    map结构中的key，一般使用字符串
         * @param class  map结构中的value缓存数据类型，Class对象，用于取值后的反序列化过程，该值支持复杂数据类型
         **/
        Integer value = CacheUtil.getFromMap("mapKey", "key", Integer.class);

        /**
         * 从Map中删除数据，方法调用前需要确保缓存中已经存放过key值为mapKey，value为Map结果的数据。
         *
         * @param mapKey 缓存索引，缓存解析器会根据mapKey于缓存中查找对应的Map结构缓存，取出后操作存储
         * @param key    map结构中的key无类型限制
         */
        CacheUtil.removeFromMap("mapKey", "key");

        /**
         * 向List中添加数据，方法调用前需要确保缓存中已经存放过key值为listKey，value为List结构的数据。
         *
         * @param listKey 缓存索引，缓存解析器会根据listKey于缓存中查找对应的List结构缓存，取出后操作存储
         * @param node    List结构中的node无类型限制
         */
        CacheUtil.addToList("listKey", "node");

        /**
         * 从List中取数据，方法调用前需要确保缓存中已经存放过key值为listKey，value为List结构的数据。
         *
         * @param listKey 缓存索引，缓存解析器会根据listKey于缓存中查找对应的List结构缓存，取出后操作取值
         * @param index   List结构中的node下标
         * @param class  map结构中的value缓存数据类型，Class对象，用于取值后的反序列化过程，该值支持复杂数据类型
         **/
        Integer value = CacheUtil.getFromList("listKey", 2, Integer.class);

        /**
         * 从List中删除数据，方法调用前需要确保缓存中已经存放过key值为listKey，value为List结构的数据。
         *
         * @param listKey 缓存索引，缓存解析器会根据listKey于缓存中查找对应的List结构缓存，取出后操作取值
         * @param index   List结构中的node下标
         */
        CacheUtil.removeFromList("mapKey", 2);

        /**
         * 向Set中添加数据，方法调用前需要确保缓存中已经存放过key值为setKey，value为Set结构的数据。
         *
         * @param setKey  缓存索引，缓存解析器会根据setKey于缓存中查找对应的Set结构缓存，取出后操作存储
         * @param node    Set结构中的node无类型限制
         */
        CacheUtil.addToSet("setKey", "node");

        /**
         * 向Set中删除数据，方法调用前需要确保缓存中已经存放过key值为setKey，value为Set结构的数据。
         *
         * @param setKey  缓存索引，缓存解析器会根据setKey于缓存中查找对应的Set结构缓存
         * @param node    Set结构中的node无类型限制
         */
        CacheUtil.removeFromSet("setKey", "node");

        /**
         * 分布式/集群同步锁，仅当缓存介质为redis情况下，该锁有使用价值，一般用于集群、分布式同步锁，如集群任务调度
         *
         * @param lockName 锁标识
         * @return 是否加锁成功
         */
        boolean isSuccess = CacheUtil.lock("lockName");

        /**
         * 过期分布式/集群同步锁，仅当缓存介质为redis情况下，该锁有使用价值，一般用于集群、分布式同步锁，如集群任务调度
         *
         * @param lockName 锁标识
         * @param timeout  锁过期时长，Duration对象，从存放时间点开始计算，过期后自动解锁
         * @return 是否加锁成功
         */
        boolean isSuccess = CacheUtil.lock("lockName", Duration.ofHours(1));

        /**
         * 分布式/集群同步锁立即解锁，仅当缓存介质为redis情况下，该锁有使用价值，一般用于集群、分布式同步锁，如集群任务调度
         *
         * @param lockName 锁标识
         */
        CacheUtil.unlock("lockName");

        /**
         * 过期分布式/集群同步锁延迟解锁，仅当缓存介质为redis情况下，该锁有使用价值，一般用于集群、分布式同步锁，如集群任务调度
         *
         * @param lockName 锁标识
         * @param timeout  锁过期时长，从调用时间点开始计算，过期后自动解锁
         * @return 是否加锁成功
         */
        CacheUtil.unlock("lockName", Duration.ofHours(1));
    }
}
```

#### 步骤 4: 缓存域

缓存分为若干区域，各缓存域间独立存取，互不干扰，程序默认缓存域为common-cache，CacheUtil工具中提供的默认方法均为直接操作默认缓存域
除默认缓存域外，开发人员可通过CacheUtil.getCache自行创建或使用自定义缓存域。缓存域也被大量用于Hibernate二级缓存。使用方法如下：

```java
public class YourClass {
    public void region(){
        // 获取名为customRegionName的缓存域，当该域不存在时，系统自行创建
        AgileCache customRegionCache = CacheUtil.getCache("customRegionName");
        
        // 直接操作缓存域内缓存，AgileCache均提供同CacheUtil一致的缓存操作方法，使用方法可直接参照CacheUtil
        Integer cacheValue = customRegionCache.get("cacheKey",Integer.class);
    }
}
```

## EhCache缓存配置

解析器中涵盖的EhCache解析器提供yml或properties形式配置，配置项与EhCache官方标准名一致，以默认公共缓存域common-cache为例：

```properties
spring.ehcache.default-config-name=common-cache
spring.ehcache.path=/temp
spring.ehcache.regions.common-cache.max-entries-local-heap=10000
spring.ehcache.regions.common-cache.max-entries-local-disk=10000000
spring.ehcache.regions.common-cache.time-to-idle-seconds=0
spring.ehcache.regions.common-cache.time-to-live-seconds=0
spring.ehcache.regions.common-cache.disk-spool-buffer-size-m-b=30
spring.ehcache.regions.common-cache.eternal=false
spring.ehcache.regions.common-cache.memory-store-eviction-policy=LRU
spring.ehcache.regions.common-cache.disk-expiry-thread-interval-seconds=120
```

## Redis缓存配置

使用spring-data-redis原生配置即可，例：

```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=123456
spring.redis.lettuce.pool.max-active=20
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.max-wait=-1
spring.redis.lettuce.pool.min-idle=0
spring.redis.lettuce.shutdown-timeout=100ms
spring.redis.ssl=false
spring.redis.database=0
spring.redis.timeout=60s
```

## Hibernate二级缓存

缓存解析器默认提供EhCache与Redis作为Hibernate二级缓存介质，缓存域工厂类如下：

* **EhCache**
  cloud.agileframework.cache.support.ehcache.EhCacheRegionFactory
* **Redis**
  cloud.agileframework.cache.support.redis.RedisRegionFactory

spring-data-jpa中配置如下：

```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    generate-ddl: false
    hibernate:
      ddl-auto: none
      naming:
        implicit-strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
    show-sql: false
    properties:
      hibernate:
        use_sql_comments: false
        format_sql: true
        cache:
          region_prefix: hibernate
          use_second_level_cache: true
          use_query_cache: true
          use_structured_entries: false
          hbm2ddl:
            auto: update
          region:
            factory_class: cloud.agileframework.cache.support.redis.RedisRegionFactory
```