package redis.clients.jedis.commands;

import redis.clients.jedis.*;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 提供一些redis常用的命令：
 */
public interface JedisCommands {


    // Connection 相关命令

    /**
     * > echo "Hello World"
     *
     * @param string
     * @return
     */
    String echo(String string);


    // Key（键）相关命令

    /**
     * > get name
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * > exists 'name'
     *
     * @param key
     * @return
     */
    Boolean exists(String key);

    /**
     * > set name 'test'
     *
     * @param key
     * @param value
     * @return
     */
    String set(String key, String value);

    /**
     * > set name 'test'
     *
     * SetParams 参数可以提供一些其他特性
     *
     * @param key
     * @param value
     * @param params
     * @return
     */
    String set(String key, String value, SetParams params);

    /**
     * > del name
     *
     * @param key
     * @return
     */
    Long del(String key);

    /**
     * > unlink name
     *
     * 该命令和DEL十分相似：删除指定的key(s),若key不存在则该key被跳过。但是，相比DEL，del会产生阻塞，而该命令会在另一个线程中回收内存，因此它是非阻塞的。
     * 这也是该命令名字的由来：仅将keys从keyspace元数据中删除，真正的删除会在后续异步操作。
     *
     * @param key
     * @return
     */
    Long unlink(String key);

    /**
     * > persist name
     *
     * 移除 key 的过期时间，key 将持久保持
     *
     * @param key
     * @return
     */
    Long persist(String key);

    /**
     * > type name
     *
     * 返回 key 所储存的值的类型
     *
     * @param key
     * @return
     */
    String type(String key);

    /**
     * 序列化给定 key ，并返回被序列化的值
     *
     * @param key
     * @return
     */
    byte[] dump(String key);

    /**
     * restore命令将dump命令序列化的值进行复原，其中ttl参数代表过期时间，如果ttl=0代表没有过期时间，例如：
     * redis-target> get hello
     * (nil)
     * redis-target> restore hello 0 "\x00\x05world\x06\x00\x8f<T\x04%\xfcNQ"
     * OK
     * redis-target> get hello
     * "world"
     *
     * @param key
     * @param ttl               过期时间，ttl=0代表没有过期时间
     * @param serializedValue
     * @return
     */
    String restore(String key, int ttl, byte[] serializedValue);

    /**
     * > restore hello 0 "\x00\x05world\x06\x00\x8f<T\x04%\xfcNQ" replace
     *
     * 指定restore回复键值对时，如果键已经存在，则报错，但是添加replace参数，不管键是否存在都会正常进行数据覆盖
     *
     * @param key
     * @param ttl
     * @param serializedValue
     * @return
     */
    String restoreReplace(String key, int ttl, byte[] serializedValue);

    /**
     * > expire name 5
     *
     * 设置过期时间
     *
     * @param key
     * @param seconds
     * @return
     */
    Long expire(String key, int seconds);

    /**
     * > pexpire name 5000
     *
     * pexpire 命令和 expire 命令的作用类似，但是它以毫秒为单位设置 key 的生存时间，而不像 EXPIRE 命令那样，以秒为单位。
     *
     * @param key
     * @param milliseconds
     * @return
     */
    Long pexpire(String key, long milliseconds);

    /**
     * > expireat hello 1599205290
     *
     * expireat 命令用于以 UNIX 时间戳(unix timestamp)格式设置 key 的过期时间, 以秒计, key 过期后将不再可用
     *
     * @param key
     * @param unixTime
     * @return
     */
    Long expireAt(String key, long unixTime);

    /**
     * > expireat hello 1599205290000
     *
     * Redis PEXPIREAT 命令用于设置 key 的过期时间，以毫秒计, key 过期后将不再可用
     *
     * @param key
     * @param millisecondsTimestamp
     * @return
     */
    Long pexpireAt(String key, long millisecondsTimestamp);

    /**
     * > ttl hello
     *
     * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)
     *
     * @param key
     * @return
     */
    Long ttl(String key);

    /**
     * > pttl hello
     *
     * 以毫秒为单位返回 key 的剩余的过期时间
     *
     * @param key
     * @return
     */
    Long pttl(String key);

    /**
     * > touch hello
     *
     * 修改某一个或多个key的最后访问时间，如果key不存在，则忽略
     *
     * @param key
     * @return
     */
    Long touch(String key);

    /**
     *
     * 假设 today_cost 是一个保存数字的列表， SORT 命令默认会返回该列表值的递增(从小到大)排序结果。
     *
     * # 将数据一一加入到列表中
     * redis> LPUSH today_cost 30
     * (integer) 1
     *
     * redis> LPUSH today_cost 1.5
     * (integer) 2
     *
     * redis> LPUSH today_cost 10
     * (integer) 3
     *
     * redis> LPUSH today_cost 8
     * (integer) 4
     *
     * redis> SORT today_cost
     * 1) "1.5"
     * 2) "8"
     * 3) "10"
     * 4) "30"
     *
     * @param key
     * @return
     */
    List<String> sort(String key);

    /**
     * sortingParameters 提供一些排序的特性，例如：升序/降序、limit、等功能
     *
     * @param key
     * @param sortingParameters
     * @return
     */
    List<String> sort(String key, SortingParams sortingParameters);

    /**
     * 将key剪切到指定索引的redis库，如果目标库已经存在该key，则返回0，表示剪切失败
     *
     * @param key
     * @param dbIndex
     * @return
     */
    Long move(String key, int dbIndex);


    // String（字符串）

    /**
     * > append name ' is my lover'
     *
     * Redis Append 命令用于为指定的 key 追加值。
     * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。
     * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。
     *
     * @param key
     * @param value
     * @return
     */
    Long append(String key, String value);

    /**
     * > substr name 0 3
     *
     * 截取字符串
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    String substr(String key, int start, int end);

    /**
     * > strlen name
     *
     * @param key
     * @return
     */
    Long strlen(String key);

    /**
     * > setbit key1 0 1
     *
     * 该命令适合一些打标的场景：
     *
     * 我们在登陆某些博客网站或者视频网站的时候，网站往往会记录我们是否阅读了某篇文章，或者是观看了某个视频。如果用传统的mysql数据库实现，
     * 如果用户数量多，文章和视频也多的情况下，那么则会给数据库带来很大的压力。而用Redis的GETBIT和SETBIT则会简单得多。我们以视频为例，
     * 我们用bitmap来记录用户们是否已经观看了某一个视频，一个视频对应一个bitmap。例如
     *
     * key:   video:1201
     * value: 000000...0000
     * key以视频英文名video+冒号+id标记。
     * value就是一个bitmap。一位(bit)有两种可能，0或者1。0代表未看，1代表已经看过了。
     * 而位置(offset)代表的就是user id。例如第200位就代表user_id为200的用户是否观看过id为1201的视频。
     *
     * 设置：SETBIT video:1201 200 1
     * 上面的命令就是设置ID为200的用户，已经看过了ID为1201的视频。
     * 查询：GETBIT video:1201 200
     * 上面的命令就是查询ID为200的用户是否观看了ID为1201的视频
     * 当然您也可以一个用户对应一个bitmap，bitmap中的位代表一个视频是否已经被观看。
     *
     * @param key
     * @param offset
     * @param value
     * @return
     */
    Boolean setbit(String key, long offset, boolean value);

    /**
     * 同{@link #setbit(String, long, boolean)}，这里的入参value只支持0或1
     *
     * @param key
     * @param offset
     * @param value
     * @return
     */
    Boolean setbit(String key, long offset, String value);

    /**
     * 获取指定键对应偏移量上的值，0或者1，这里返回false或者true
     *
     * @param key
     * @param offset
     * @return
     */
    Boolean getbit(String key, long offset);

    /**
     * 统计key对应的bit值中，包含的1的数量，例如：
     *
     * 127.0.0.1:6379> set testKey "a"
     * OK
     * 127.0.0.1:6379> set testKey1 "c"
     * OK
     * 127.0.0.1:6379> bitcount testKey
     * (integer) 3
     * 127.0.0.1:6379> bitcount testKey1
     * (integer) 4
     *
     * 这里a对应的ASCII值为97（二进制：01100001），所以 bitcount命令返回包含1的个数是3
     * 这里c对应的ASCII值为99（二进制：01100011），所以 bitcount命令返回包含1的个数是4
     *
     * @param key
     * @return
     */
    Long bitcount(String key);

    /**
     * bitcount命令：
     * 语法：bitcount key [start] [end]
     * 计算给定字符串中,被甚至为1的比特位的数量。
     * 默认情况下,给定的整个字符串都会被进行计数,可以通过start、end指定区间中指定计数操作
     * start和end可以包含负值,以便从字符串末尾开始索引字节,其中-1是最后一个字节,以此类推
     * 不存在的键被视为空字符串,返回0
     * 返回值：
     * 被设置为1的位的数量。
     * 举例：
     * set m "ab"      0110000101100010
     * bitcount m      返回6
     * bitcount m 0 0  也就是a 返回3
     * bitcount m 1 1  也就是b 返回3
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    Long bitcount(String key, long start, long end);

    /**
     * 用指定的字符串覆盖给定 key 所储存的字符串值，覆盖的位置从偏移量 offset 开始：
     * redis 127.0.0.1:6379> set key1 "Hello World"
     * OK
     * redis 127.0.0.1:6379> setrange key1 6 "Redis"
     * (integer) 11
     * redis 127.0.0.1:6379> get key1
     * "Hello Redis"
     *
     * @param key
     * @param offset
     * @param value
     * @return
     */
    Long setrange(String key, long offset, String value);

    /**
     * 用于获取存储在指定 key 中字符串的子字符串。字符串的截取范围由 start 和 end 两个偏移量决定(包括 start 和 end 在内)。
     * redis 127.0.0.1:6379> SET mykey "This is my test key"
     * OK
     * redis 127.0.0.1:6379> GETRANGE mykey 0 3
     * "This"
     * redis 127.0.0.1:6379> GETRANGE mykey 0 -1
     * "This is my test key"
     *
     * @param key
     * @param startOffset
     * @param endOffset
     * @return
     */
    String getrange(String key, long startOffset, long endOffset);

    /**
     * 用于设置指定 key 的值，并返回 key 的旧值:
     * redis> GETSET db mongodb    # 没有旧值，返回 nil
     * (nil)
     * <p>
     * redis> GET db
     * "mongodb"
     * <p>
     * redis> GETSET db redis      # 返回旧值 mongodb
     * "mongodb"
     * <p>
     * redis> GET db
     * "redis"
     *
     * @param key
     * @param value
     * @return
     */
    String getSet(String key, String value);

    /**
     * Redis Setnx（SET if Not eXists） 命令在指定的 key 不存在时，为 key 设置指定的值。
     * redis> EXISTS job                # job 不存在
     * (integer) 0
     * <p>
     * redis> SETNX job "programmer"    # job 设置成功
     * (integer) 1
     * <p>
     * redis> SETNX job "code-farmer"   # 尝试覆盖 job ，失败
     * (integer) 0
     * <p>
     * redis> GET job                   # 没有被覆盖
     * "programmer"
     *
     * @param key
     * @param value
     * @return
     */
    Long setnx(String key, String value);

    /**
     * Redis Setex 命令为指定的 key 设置值及其过期时间。如果 key 已经存在， SETEX 命令将会替换旧的值。
     * redis 127.0.0.1:6379> SETEX mykey 60 redis
     * OK
     * redis 127.0.0.1:6379> TTL mykey
     * 60
     * redis 127.0.0.1:6379> GET mykey
     * "redis
     *
     * @param key
     * @param seconds
     * @param value
     * @return
     */
    String setex(String key, int seconds, String value);

    /**
     * Redis Psetex 命令以毫秒为单位设置 key 的生存时间：
     * redis 127.0.0.1:6379> PSETEX mykey 1000 "Hello"
     * OK
     * redis 127.0.0.1:6379> PTTL mykey
     * 999
     * redis 127.0.0.1:6379> GET mykey
     * 1) "Hello"
     *
     * @param key
     * @param milliseconds
     * @param value
     * @return
     */
    String psetex(String key, long milliseconds, String value);

    /**
     * Redis Decrby 命令将 key 所储存的值减去指定的减量值，如果key不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误，本操作的值限制在 64 位(bit)有符号数字表示之内。
     *
     * # 对已存在的 key 进行 DECRBY
     *
     * redis> SET count 100
     * OK
     *
     * redis> DECRBY count 20
     * (integer) 80
     *
     * # 对不存在的 key 进行DECRBY
     *
     * redis> EXISTS pages
     * (integer) 0
     *
     * redis> DECRBY pages 10
     * (integer) -10
     *
     * @param key
     * @param decrement
     * @return
     */
    Long decrBy(String key, long decrement);

    /**
     * Redis Decr 命令将key中储存的数字值减一，如果key不存在，那么key的值会先被初始化为0，然后再执行 DECR 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误，本操作的值限制在 64 位(bit)有符号数字表示之内。
     *
     * # 对存在的数字值 key 进行 DECR
     * redis> SET failure_times 10
     * OK
     *
     * redis> DECR failure_times
     * (integer) 9
     *
     * # 对不存在的 key 值进行 DECR
     *
     * redis> EXISTS count
     * (integer) 0
     *
     * redis> DECR count
     * (integer) -1
     *
     * # 对存在但不是数值的 key 进行 DECR
     *
     * redis> SET company YOUR_CODE_SUCKS.LLC
     * OK
     *
     * redis> DECR company
     * (error) ERR value is not an integer or out of range
     *
     * @param key
     * @return
     */
    Long decr(String key);

    /**
     * 给指定key的value加上对应的数值
     *
     * @param key
     * @param increment
     * @return
     */
    Long incrBy(String key, long increment);

    /**
     * 给指定key的value加上对应的数值
     *
     * @param key
     * @param increment
     * @return
     */
    Double incrByFloat(String key, double increment);

    /**
     * 对于给定key的value进行自增+1
     *
     * @param key
     * @return
     */
    Long incr(String key);

    /***
     * 用来获取二进制位串中第一个1或者0的位置，例如：
     * 127.0.0.1:6379> set key_0 0
     * OK
     * 127.0.0.1:6379> bitpos key_0 1
     * (integer) 2
     * 127.0.0.1:6379> bitpos key_0 0
     * (integer) 0
     *
     * 0对应的ASCII二进制码为：00110000
     *
     *
     * @param key
     * @param value
     * @return
     */
    Long bitpos(String key, boolean value);

    /**
     * 127.0.0.1:6379> set key_a0 'a0'
     * OK
     * 127.0.0.1:6379> bitpos key_a0 1 1
     * (integer) 10
     *
     * 'a0'对应的ASCII码为：01100001 00110000
     * bitpos key_a0 1 1 命令表示从第二个子节点开始计算，第一个bit值为1的索引位置
     *
     * @param key
     * @param value
     * @param params
     * @return
     */
    Long bitpos(String key, boolean value, BitPosParams params);

    // TODO whz 比较复杂
    List<Long> bitfield(String key, String... arguments);
    List<Long> bitfieldReadonly(String key, String... arguments);


    // Hash（哈希表）

    /**
     * 127.0.0.1:6379> hset user_1 name zhangsan
     * (integer) 1
     * 127.0.0.1:6379> hset user_1 age 24
     * (integer) 1
     *
     * 将哈希表key中的域field的值设为value：如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作，如果域 field 已经存在于哈希表中，旧值将被覆盖。
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hset(String key, String field, String value);

    /**
     * 同hset批量设置
     *
     * @param key
     * @param hash
     * @return
     */
    Long hset(String key, Map<String, String> hash);

    /**
     * 127.0.0.1:6379> hset user_1 name zhangsan
     * (integer) 1
     * 127.0.0.1:6379> hget user_1 name
     * "zhangsan"
     *
     * @param key
     * @param field
     * @return
     */
    String hget(String key, String field);

    /**
     * 仅在对应field没有值时，set生效:
     * 127.0.0.1:6379> hset user_1 name zhangsan
     * (integer) 0
     * 127.0.0.1:6379> hsetnx user_1 name lisi
     * (integer) 0
     * 127.0.0.1:6379> hsetnx user_1 sex 1
     * (integer) 1
     * 127.0.0.1:6379> hget user_1 name
     * "zhangsan"
     * 127.0.0.1:6379> hget user_1 sex
     * "1"
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hsetnx(String key, String field, String value);

    /**
     * 在Redis使用过程中，发现Redis hash的两个指令HSET和HMSET非常类似，搜索了一下，差别在于：HSET/HMSET将单个/多个field - value(域-值)对设置到哈希表key中，然而在使用时HSET也可以做到。
     * 127.0.0.1:6379[1]> hset people name Sam age 28 sex male
     * (integer) 3
     * 127.0.0.1:6379[1]> hmset people1 name Anny age 27 sex female
     * OK
     *
     * 结果并没有什么差异：
     * 127.0.0.1:6379[1]> hgetall people
     * 1) "name"
     * 2) "Sam"
     * 3) "age"
     * 4) "28"
     * 5) "sex"
     * 6) "male"
     * 127.0.0.1:6379[1]> hgetall people1
     * 1) "name"
     * 2) "Anny"
     * 3) "age"
     * 4) "27"
     * 5) "sex"
     * 6) "female"
     *
     * 后来在官方文档中发现：根据Redis 4.0.0，HMSET被视为已弃用。请在新代码中使用HSET，在这之前HSET只能设置单个键值对，同时设置多个时必须使用HMSET。而现在HSET也允许接受多个键值对作参数了。
     *
     *
     * @param key
     * @param hash
     * @return
     */
    String hmset(String key, Map<String, String> hash);

    /**
     * 批量获取多个field的值
     *
     * @param key
     * @param fields
     * @return
     */
    List<String> hmget(String key, String... fields);

    /**
     * 获取指定field的字符串长度
     *
     * @param key
     * @param field
     * @return length of the value for key
     */
    Long hstrlen(String key, String field);

    /**
     * 给对应的field的值加上对应的数值
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hincrBy(String key, String field, long value);

    /**
     * 同hincrBy
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Double hincrByFloat(String key, String field, double value);

    /**
     * 判断给定的field是否存在
     *
     * @param key
     * @param field
     * @return
     */
    Boolean hexists(String key, String field);

    /**
     * 删除给定的field
     *
     * @param key
     * @param field
     * @return
     */
    Long hdel(String key, String... field);

    /**
     * 127.0.0.1:6379> hset people name Sam age 28 sex male
     * (integer) 3
     * 127.0.0.1:6379> hgetall people
     * 1) "name"
     * 2) "Sam"
     * 3) "age"
     * 4) "28"
     * 5) "sex"
     * 6) "male"
     * 127.0.0.1:6379> hlen people
     * (integer) 3
     *
     * 获取key下的field的个数
     *
     * @param key
     * @return
     */
    Long hlen(String key);

    /**
     * 127.0.0.1:6379> hkeys people
     * 1) "name"
     * 2) "age"
     * 3) "sex"
     *
     * 获取指定key下的field
     *
     * @param key
     * @return
     */
    Set<String> hkeys(String key);

    /**
     * 127.0.0.1:6379> hvals people
     * 1) "Sam"
     * 2) "28"
     * 3) "male"
     *
     * 获取指定key下的field字段值
     *
     * @param key
     * @return
     */
    List<String> hvals(String key);

    /**
     * 127.0.0.1:6379> hgetall people
     * 1) "name"
     * 2) "Sam"
     * 3) "age"
     * 4) "28"
     * 5) "sex"
     * 6) "male"
     *
     * 获取key下的所有field的键值对
     *
     * @param key
     * @return
     */
    Map<String, String> hgetAll(String key);

    /**
     * 当我们需要遍历Redis所有key或者指定模式的key时，首先想到的是KEYS命令，但是如果redis数据非常大，并且key也非常多的情况下，
     * 查询的时候很可能会很慢，造成整个redis阻塞，那么有什么办法解决呢？
     * 当然有了，今天就简单的介绍一下，如：scan和hscan
     *
     * 格式如下：
     *
     * SCAN cursor [MATCH pattern] [COUNT count]
     * HSCAN key cursor [MATCH pattern] [COUNT count]
     *
     * SCAN命令是一个基于游标的迭代器, 这意味着命令每次被调用都需要使用上一次这个调用返回的游标作为该次调用的游标参数，以此来延续之前的迭
     * 代过程, 当SCAN命令的游标参数被设置为 0 时， 服务器将开始一次新的迭代， 而当服务器向用户返回值为 0 的游标时， 表示迭代已结束，HSCAN同SCAN命令相同。
     *
     * 1，查看一下hash有多少条记录
     * 127.0.0.1:6379[1]> hgetall pms:1
     *  1) "stock"
     *  2) "12"
     *  3) "freeze"
     *  4) "10"
     *  5) "stock:1"
     *  6) "11"
     *  7) "stock:2"
     *  8) "23"
     *  9) "stock:freeze:1"
     * 10) "111"
     * 11) "stock:5"
     * 12) "1212"
     *
     * 2，模糊查看pms:1下的键
     * 127.0.0.1:6379[1]> hscan pms:1 0 match stock:* count 3
     * 1) "0"
     * 2) 1) "stock:1"
     *    2) "11"
     *    3) "stock:2"
     *    4) "23"
     *
     *
     * @param key
     * @param cursor
     * @return
     */
    ScanResult<Map.Entry<String, String>> hscan(String key, String cursor);
    ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params);


    // List（列表）

    /**
     *
     *
     * @param key
     * @param string
     * @return
     */
    Long rpush(String key, String... string);

    Long lpush(String key, String... string);

    Long llen(String key);

    List<String> lrange(String key, long start, long stop);

    String ltrim(String key, long start, long stop);

    String lindex(String key, long index);

    String lset(String key, long index, String value);

    Long lrem(String key, long count, String value);

    String lpop(String key);

    String rpop(String key);

    Long linsert(String key, ListPosition where, String pivot, String value);

    Long lpushx(String key, String... string);

    Long rpushx(String key, String... string);

    List<String> blpop(int timeout, String key);

    List<String> brpop(int timeout, String key);


    // Set（集合）

    Long sadd(String key, String... member);

    Set<String> smembers(String key);

    Long srem(String key, String... member);

    String spop(String key);

    Set<String> spop(String key, long count);

    Long scard(String key);

    Boolean sismember(String key, String member);

    String srandmember(String key);

    List<String> srandmember(String key, int count);

    ScanResult<String> sscan(String key, String cursor);

    ScanResult<String> sscan(String key, String cursor, ScanParams params);


    // 有序集合

    Long zadd(String key, double score, String member);

    Long zadd(String key, double score, String member, ZAddParams params);

    Long zadd(String key, Map<String, Double> scoreMembers);

    Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

    Set<String> zrange(String key, long start, long stop);

    Long zrem(String key, String... members);

    Double zincrby(String key, double increment, String member);

    Double zincrby(String key, double increment, String member, ZIncrByParams params);

    Long zrank(String key, String member);

    Long zrevrank(String key, String member);

    Set<String> zrevrange(String key, long start, long stop);

    Set<Tuple> zrangeWithScores(String key, long start, long stop);

    Set<Tuple> zrevrangeWithScores(String key, long start, long stop);

    Long zcard(String key);

    Double zscore(String key, String member);

    Tuple zpopmax(String key);

    Set<Tuple> zpopmax(String key, int count);

    Tuple zpopmin(String key);

    Set<Tuple> zpopmin(String key, int count);

    Long zcount(String key, double min, double max);

    Long zcount(String key, String min, String max);

    Set<String> zrangeByScore(String key, double min, double max);

    Set<String> zrangeByScore(String key, String min, String max);

    Set<String> zrevrangeByScore(String key, double max, double min);

    Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

    Set<String> zrevrangeByScore(String key, String max, String min);

    Set<String> zrangeByScore(String key, String min, String max, int offset, int count);

    Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

    Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);

    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

    Long zremrangeByRank(String key, long start, long stop);

    Long zremrangeByScore(String key, double min, double max);

    Long zremrangeByScore(String key, String min, String max);

    Long zlexcount(String key, String min, String max);

    Set<String> zrangeByLex(String key, String min, String max);

    Set<String> zrangeByLex(String key, String min, String max, int offset, int count);

    Set<String> zrevrangeByLex(String key, String max, String min);

    Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);

    Long zremrangeByLex(String key, String min, String max);

    ScanResult<Tuple> zscan(String key, String cursor);

    ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);


    // HyperLogLog

    Long pfadd(String key, String... elements);
    long pfcount(String key);


    // Geo Commands

    Long geoadd(String key, double longitude, double latitude, String member);

    Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap);

    Double geodist(String key, String member1, String member2);

    Double geodist(String key, String member1, String member2, GeoUnit unit);

    List<String> geohash(String key, String... members);

    List<GeoCoordinate> geopos(String key, String... members);

    List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);


    // Redis5.0 新特性：Streams数据结构结构相关命令

    /**
     * XADD key ID field string [field string ...]
     *
     * @param key
     * @param id
     * @param hash
     * @return the ID of the added entry
     */
    StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash);

    /**
     * XADD key MAXLEN ~ LEN ID field string [field string ...]
     *
     * @param key
     * @param id
     * @param hash
     * @param maxLen
     * @param approximateLength
     * @return
     */
    StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash, long maxLen, boolean approximateLength);

    /**
     * XLEN key
     *
     * @param key
     * @return
     */
    Long xlen(String key);

    /**
     * XRANGE key start end [COUNT count]
     *
     * @param key
     * @param start minimum {@link StreamEntryID} for the retrieved range, passing <code>null</code> will indicate minimum ID possible in the stream
     * @param end   maximum {@link StreamEntryID} for the retrieved range, passing <code>null</code> will indicate maximum ID possible in the stream
     * @param count maximum number of entries returned
     * @return The entries with IDs matching the specified range.
     */
    List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count);

    /**
     * XREVRANGE key end start [COUNT <n>]
     *
     * @param key
     * @param start minimum {@link StreamEntryID} for the retrieved range, passing <code>null</code> will indicate minimum ID possible in the stream
     * @param end   maximum {@link StreamEntryID} for the retrieved range, passing <code>null</code> will indicate maximum ID possible in the stream
     * @param count The entries with IDs matching the specified range.
     * @return the entries with IDs matching the specified range, from the higher ID to the lower ID matching.
     */
    List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count);

    /**
     * XACK key group ID [ID ...]
     *
     * @param key
     * @param group
     * @param ids
     * @return
     */
    long xack(String key, String group, StreamEntryID... ids);

    /**
     * XGROUP CREATE <key> <groupname> <id or $>
     *
     * @param key
     * @param groupname
     * @param id
     * @param makeStream
     * @return
     */
    String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream);

    /**
     * XGROUP SETID <key> <groupname> <id or $>
     *
     * @param key
     * @param groupname
     * @param id
     * @return
     */
    String xgroupSetID(String key, String groupname, StreamEntryID id);

    /**
     * XGROUP DESTROY <key> <groupname>
     *
     * @param key
     * @param groupname
     * @return
     */
    long xgroupDestroy(String key, String groupname);

    /**
     * XGROUP DELCONSUMER <key> <groupname> <consumername>
     *
     * @param key
     * @param groupname
     * @param consumername
     * @return
     */
    Long xgroupDelConsumer(String key, String groupname, String consumername);

    /**
     * XPENDING key group [start end count] [consumer]
     *
     * @param key
     * @param groupname
     * @param start
     * @param end
     * @param count
     * @param consumername
     * @return
     */
    List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end, int count, String consumername);

    /**
     * XDEL key ID [ID ...]
     *
     * @param key
     * @param ids
     * @return
     */
    long xdel(String key, StreamEntryID... ids);

    /**
     * XTRIM key MAXLEN [~] count
     *
     * @param key
     * @param maxLen
     * @param approximate
     * @return
     */
    long xtrim(String key, long maxLen, boolean approximate);

    /**
     * XCLAIM <key> <group> <consumer> <min-idle-time> <ID-1> <ID-2>
     * [IDLE <milliseconds>] [TIME <mstime>] [RETRYCOUNT <count>]
     * [FORCE] [JUSTID]
     */
    List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, long newIdleTime, int retries, boolean force, StreamEntryID... ids);

    /**
     * Introspection command used in order to retrieve different information about the stream
     *
     * @param key Stream name
     * @return {@link StreamInfo} that contains information about the stream
     */
    StreamInfo xinfoStream(String key);

    /**
     * Introspection command used in order to retrieve different information about groups in the stream
     *
     * @param key Stream name
     * @return List of {@link StreamGroupInfo} containing information about groups
     */
    List<StreamGroupInfo> xinfoGroup(String key);

    /**
     * Introspection command used in order to retrieve different information about consumers in the group
     *
     * @param key   Stream name
     * @param group Group name
     * @return List of {@link StreamConsumersInfo} containing information about consumers that belong
     * to the the group
     */
    List<StreamConsumersInfo> xinfoConsumers(String key, String group);
}
