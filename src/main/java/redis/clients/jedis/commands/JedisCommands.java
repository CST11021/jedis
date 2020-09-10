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
     * > rpush names zhangsan lisi
     *
     *
     *
     * @param key
     * @param string
     * @return
     */
    Long rpush(String key, String... string);

    /**
     *
     *
     * @param key
     * @param string
     * @return
     */
    Long lpush(String key, String... string);

    /**
     * 127.0.0.1:6379> rpush names zhangsan lisi
     * (integer) 2
     * 127.0.0.1:6379> llen names
     * (integer) 2
     *
     * 获取列表的元素个数
     *
     * @param key
     * @return
     */
    Long llen(String key);

    /**
     * 127.0.0.1:6379> lrange names 0 5
     * 1) "wangwu"
     * 2) "zhangsan"
     * 3) "lisi"
     *
     * 获取指定索引返回内的元素，以从左到右的顺序返回
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    List<String> lrange(String key, long start, long stop);

    /**
     * 127.0.0.1:6379> lrange names 0 5
     * 1) "wangwu"
     * 2) "zhangsan"
     * 3) "lisi"
     * 4) "liuliu"
     * 127.0.0.1:6379> ltrim names 1 2
     * OK
     * 127.0.0.1:6379> lrange names 0 5
     * 1) "zhangsan"
     * 2) "lisi"
     *
     * 保留指定区间的元素，区间外的元素从列表中移除
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    String ltrim(String key, long start, long stop);

    /**
     * 127.0.0.1:6379> lindex names 0
     * "zhangsan"
     *
     * 获取指定索引位置的元素
     *
     * @param key
     * @param index
     * @return
     */
    String lindex(String key, long index);

    /**
     * 127.0.0.1:6379> lset names 5 wangwu
     * (error) ERR index out of range
     * 127.0.0.1:6379> lset names 0 wangwu
     * OK
     *
     * 替换索引位置的元素，如果索引超出列表的范围，则抛出异常
     *
     * @param key
     * @param index
     * @param value
     * @return
     */
    String lset(String key, long index, String value);

    /**
     * # 先创建一个表，内容排列是
     * # morning hello morning helllo morning
     *
     * redis> LPUSH greet "morning"
     * (integer) 1
     * redis> LPUSH greet "hello"
     * (integer) 2
     * redis> LPUSH greet "morning"
     * (integer) 3
     * redis> LPUSH greet "hello"
     * (integer) 4
     * redis> LPUSH greet "morning"
     * (integer) 5
     *
     * redis> LRANGE greet 0 4         # 查看所有元素
     * 1) "morning"
     * 2) "hello"
     * 3) "morning"
     * 4) "hello"
     * 5) "morning"
     *
     * redis> LREM greet 2 morning     # 移除从表头到表尾，最先发现的两个 morning
     * (integer) 2                     # 两个元素被移除
     *
     * redis> LLEN greet               # 还剩 3 个元素
     * (integer) 3
     *
     * redis> LRANGE greet 0 2
     * 1) "hello"
     * 2) "hello"
     * 3) "morning"
     *
     * redis> LREM greet -1 morning    # 移除从表尾到表头，第一个 morning
     * (integer) 1
     *
     * redis> LLEN greet               # 剩下两个元素
     * (integer) 2
     *
     * redis> LRANGE greet 0 1
     * 1) "hello"
     * 2) "hello"
     *
     * redis> LREM greet 0 hello      # 移除表中所有 hello
     * (integer) 2                    # 两个 hello 被移除
     *
     * redis> LLEN greet
     * (integer) 0
     *
     *
     * 移除与value值相等元素
     *
     * @param key       key
     * @param count     移除的个数
     * @param value     要移除的元素
     * @return
     */
    Long lrem(String key, long count, String value);

    /**
     * 127.0.0.1:6379> lpop names
     * "wangwu"
     *
     * 从列表左边开始，返回第一元素，并从列表中移除
     * @param key
     * @return
     */
    String lpop(String key);

    /**
     * 127.0.0.1:6379> rpop names
     * "wangwu"
     *
     * 从列表右边开始，返回第一元素，并从列表中移除
     *
     * @param key
     * @return
     */
    String rpop(String key);

    /**
     * redis> RPUSH mylist "Hello"
     * (integer) 1
     *
     * redis> RPUSH mylist "World"
     * (integer) 2
     *
     * redis> LINSERT mylist BEFORE "World" "There"
     * (integer) 3
     *
     * redis> LRANGE mylist 0 -1
     * 1) "Hello"
     * 2) "There"
     * 3) "World"
     *
     *
     * # 对一个非空列表插入，查找一个不存在的 pivot
     *
     * redis> LINSERT mylist BEFORE "go" "let's"
     * (integer) -1                                    # 失败
     *
     *
     *
     * 将值 value 插入到列表 key 当中，位于值 pivot 之前或之后
     *
     *
     * @param key
     * @param where
     * @param pivot
     * @param value
     * @return
     */
    Long linsert(String key, ListPosition where, String pivot, String value);

    /**
     * 将值 value 插入到列表 key 的表头，当且仅当 key 存在并且是一个列表
     *
     * @param key
     * @param string
     * @return
     */
    Long lpushx(String key, String... string);

    /**
     * 将值 value 插入到列表 key 的表尾，当且仅当 key 存在并且是一个列表
     *
     * @param key
     * @param string
     * @return
     */
    Long rpushx(String key, String... string);

    /**
     * 同lpop命令，只不过blpop命令是阻塞的，如果列表key不存在，或者列表为空，则该命令会一直阻塞，直到另一客户端插入一个元素
     *
     * redis> EXISTS job                # 确保两个 key 都不存在
     * (integer) 0
     * redis> EXISTS command
     * (integer) 0
     *
     * redis> BLPOP job command 300     # 因为key一开始不存在，所以操作会被阻塞，直到另一客户端对 job 或者 command 列表进行 PUSH 操作。
     * 1) "job"                         # 这里被 push 的是 job
     * 2) "do my home work"             # 被弹出的值
     * (26.26s)                         # 等待的秒数
     *
     * redis> BLPOP job command 5       # 等待超时的情况
     * (nil)
     * (5.66s)                          # 等待的秒数
     *
     *
     * @param timeout   阻塞时等待的时间，单位为秒
     * @param key
     * @return
     */
    List<String> blpop(int timeout, String key);

    /**
     * 同blpop命令
     *
     * @param timeout
     * @param key
     * @return
     */
    List<String> brpop(int timeout, String key);


    // Set（集合）

    /**
     * 127.0.0.1:6379> sadd numbers 1 2 8 3 5
     * (integer) 5
     *
     * 将元素放入集合中，集合是无序的
     *
     * @param key
     * @param member
     * @return
     */
    Long sadd(String key, String... member);

    /**
     * 127.0.0.1:6379> sadd numbers 1 2 8 3 5
     * (integer) 5
     * 127.0.0.1:6379> smembers numbers
     * 1) "1"
     * 2) "2"
     * 3) "3"
     * 4) "5"
     * 5) "8"
     *
     * 获取集合内的元素
     *
     * @param key
     * @return
     */
    Set<String> smembers(String key);

    /**
     * # 测试数据
     *
     * redis> SMEMBERS languages
     * 1) "c"
     * 2) "lisp"
     * 3) "python"
     * 4) "ruby"
     *
     *
     * # 移除单个元素
     *
     * redis> SREM languages ruby
     * (integer) 1
     *
     *
     * # 移除不存在元素
     *
     * redis> SREM languages non-exists-language
     * (integer) 0
     *
     *
     * # 移除多个元素
     *
     * redis> SREM languages lisp python c
     * (integer) 3
     *
     * redis> SMEMBERS languages
     * (empty list or set)
     *
     *
     * 移除集合中的元素
     *
     * @param key
     * @param member
     * @return
     */
    Long srem(String key, String... member);

    /**
     * 127.0.0.1:6379> smembers numbers
     * 1) "1"
     * 2) "2"
     * 3) "3"
     * 4) "5"
     * 5) "8"
     * 127.0.0.1:6379> spop numbers
     * "3"
     *
     * 移除并返回集合中的一个随机元素
     *
     * @param key
     * @return
     */
    String spop(String key);

    /**
     * 127.0.0.1:6379> spop numbers 3
     * 1) "8"
     * 2) "5"
     * 3) "1"
     *
     * 移除并返回集合中的count个随机元素
     *
     * @param key
     * @param count
     * @return
     */
    Set<String> spop(String key, long count);

    /**
     * redis> SADD tool pc printer phone
     * (integer) 3
     *
     * redis> SCARD tool   # 非空集合
     * (integer) 3
     *
     * redis> DEL tool
     * (integer) 1
     *
     * redis> SCARD tool   # 空集合
     * (integer) 0
     *
     * 获取集合的基数（即集合中的元素个数）
     *
     * @param key
     * @return
     */
    Long scard(String key);

    /**
     * 127.0.0.1:6379> sismember numbers 2
     * (integer) 1
     *
     * 判断 member 元素是否集合 key 的成员
     *
     * @param key
     * @param member
     * @return
     */
    Boolean sismember(String key, String member);

    /**
     * 127.0.0.1:6379> srandmember numbers
     * "2"
     *
     * 随机返回集合中的一个元素，但不删除
     *
     * @param key
     * @return
     */
    String srandmember(String key);

    /**
     * 127.0.0.1:6379> spop numbers 3
     * 1) "8"
     * 2) "5"
     * 3) "1"
     *
     * 随机返回集合中的count个元素，但不删除
     *
     * @param key
     * @param count
     * @return
     */
    List<String> srandmember(String key, int count);

    /**
     * 迭代集合中的元素
     *
     * @param key
     * @param cursor
     * @return
     */
    ScanResult<String> sscan(String key, String cursor);
    ScanResult<String> sscan(String key, String cursor, ScanParams params);


    // 有序集合

    /**
     * # 添加单个元素
     *
     * redis> ZADD page_rank 10 google.com
     * (integer) 1
     *
     *
     * # 添加多个元素
     *
     * redis> ZADD page_rank 9 baidu.com 8 bing.com
     * (integer) 2
     *
     * redis> ZRANGE page_rank 0 -1 WITHSCORES
     * 1) "bing.com"
     * 2) "8"
     * 3) "baidu.com"
     * 4) "9"
     * 5) "google.com"
     * 6) "10"
     *
     *
     * # 添加已存在元素，且 score 值不变
     *
     * redis> ZADD page_rank 10 google.com
     * (integer) 0
     *
     * redis> ZRANGE page_rank 0 -1 WITHSCORES  # 没有改变
     * 1) "bing.com"
     * 2) "8"
     * 3) "baidu.com"
     * 4) "9"
     * 5) "google.com"
     * 6) "10"
     *
     *
     * # 添加已存在元素，但是改变 score 值
     *
     * redis> ZADD page_rank 6 bing.com
     * (integer) 0
     *
     * redis> ZRANGE page_rank 0 -1 WITHSCORES  # bing.com 元素的 score 值被改变
     * 1) "bing.com"
     * 2) "6"
     * 3) "baidu.com"
     * 4) "9"
     * 5) "google.com"
     * 6) "10"
     *
     * 添加一个元素和其score值到有序集合中
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    Long zadd(String key, double score, String member);
    Long zadd(String key, double score, String member, ZAddParams params);
    Long zadd(String key, Map<String, Double> scoreMembers);
    /**
     * 添加多个元素和其score值到有序集合中
     *
     * @param key
     * @param scoreMembers
     * @param params
     * @return
     */
    Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

    /**
     * 127.0.0.1:6379> zadd numbers 1 1 2 2 3 3 4 4 5 5
     * (integer) 5
     * 127.0.0.1:6379> zrange numbers 0 2
     * 1) "1"
     * 2) "2"
     * 3) "3"
     *
     * 获取指定区间内的集合元素
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    Set<String> zrange(String key, long start, long stop);

    /**
     * 移除有序集key中的一个或多个成员，不存在的成员将被忽略
     *
     * @param key
     * @param members
     * @return
     */
    Long zrem(String key, String... members);

    /**
     * 127.0.0.1:6379> zadd numbers 1 1 2 2 3 3 4 4 5 5
     * (integer) 5
     * 127.0.0.1:6379> ZINCRBY numbers 3 5
     * "8"
     *
     * 为有序集 key 的成员 member 的 score 值加上增量 increment
     *
     * @param key
     * @param increment
     * @param member
     * @return
     */
    Double zincrby(String key, double increment, String member);
    Double zincrby(String key, double increment, String member, ZIncrByParams params);

    /**
     * 返回有序集 key 中成员 member 的排名，其中有序集成员按 score 值递增(从小到大)顺序排列
     *
     * redis> ZRANGE salary 0 -1 WITHSCORES        # 显示所有成员及其 score 值
     * 1) "peter"
     * 2) "3500"
     * 3) "tom"
     * 4) "4000"
     * 5) "jack"
     * 6) "5000"
     *
     * redis> ZRANK salary tom                     # 显示 tom 的薪水排名，第二
     * (integer) 1
     *
     *
     * @param key
     * @param member
     * @return
     */
    Long zrank(String key, String member);

    /**
     * redis 127.0.0.1:6379> ZRANGE salary 0 -1 WITHSCORES     # 测试数据
     * 1) "jack"
     * 2) "2000"
     * 3) "peter"
     * 4) "3500"
     * 5) "tom"
     * 6) "5000"
     *
     * redis> ZREVRANK salary peter     # peter 的工资排第二
     * (integer) 1
     *
     * redis> ZREVRANK salary tom       # tom 的工资最高
     * (integer) 0
     *
     * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序。
     *
     * @param key
     * @param member
     * @return
     */
    Long zrevrank(String key, String member);

    /**
     * 对指定范围的元素进行排序，其成员按 score 值递减(从大到小)排序
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    Set<String> zrevrange(String key, long start, long stop);

    /**
     * redis 127.0.0.1:6379> ZRANGE salary 0 -1 WITHSCORES     # 测试数据
     * 1) "jack"
     * 2) "2000"
     * 3) "peter"
     * 4) "3500"
     * 5) "tom"
     * 6) "5000"
     *
     * 返回有序集合指定区间内的元素，并携带分数
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    Set<Tuple> zrangeWithScores(String key, long start, long stop);

    /**
     * 对指定范围的元素进行排序，其成员按 score 值递减(从大到小)排序，并且带分数
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    Set<Tuple> zrevrangeWithScores(String key, long start, long stop);

    /**
     * 当 key 存在且是有序集类型时，返回有序集的基数。 当 key 不存在时，返回 0 。
     *
     * redis > ZADD salary 2000 tom    # 添加一个成员
     * (integer) 1
     *
     * redis > ZCARD salary
     * (integer) 1
     *
     * redis > ZADD salary 5000 jack   # 再添加一个成员
     * (integer) 1
     *
     * redis > ZCARD salary
     * (integer) 2
     *
     * redis > EXISTS non_exists_key   # 对不存在的 key 进行 ZCARD 操作
     * (integer) 0
     *
     * redis > ZCARD non_exists_key
     * (integer) 0
     *
     *
     * @param key
     * @return
     */
    Long zcard(String key);

    /**
     * redis> ZRANGE salary 0 -1 WITHSCORES    # 测试数据
     * 1) "tom"
     * 2) "2000"
     * 3) "peter"
     * 4) "3500"
     * 5) "jack"
     * 6) "5000"
     *
     * redis> ZSCORE salary peter              # 注意返回值是字符串
     * "3500"
     *
     * 返回有序集 key 中，成员 member 的 score 值。
     *
     * @param key
     * @param member
     * @return
     */
    Double zscore(String key, String member);

    /**
     * 有序集合（SortedSet又称zset）是 Redis 的老牌数据结构之一， 它的 API 一直以来都是很稳定的， 基本上没有发生过变化，
     * 但随着 Redis 5 的到来， 新版本 Redis 也给它加上了四个新的命令， 这些命令分别是：ZPOPMAX、ZPOPMIN、BZPOPMAX、BZPOPMIN
     *
     * 其中， ZPOPMAX 命令用于移除并弹出有序集合中分值最大的 count 个元素：
     * 而 ZPOPMIN 命令则用于移除并弹出有序集合中分值最小的 count 个元素：
     * BZPOPMAX 和 BZPOPMIN 是上述两个命令的阻塞变种， 这两个命令每次只能弹出单个元素， 但可以接受多个键作为被弹出的对象， 并且需要使用 timeout 参数去指定命令的最长阻塞时间：
     * BZPOPMAX 命令和 BZPOPMIN 命令的行为跟 BLPOP 、 BRPOP 等命令的语义非常相似， 熟悉上述两个命令的读者应该不会对这两个新命令感到陌生。
     *
     *
     * @param key
     * @return
     */
    Tuple zpopmax(String key);
    Set<Tuple> zpopmax(String key, int count);
    Tuple zpopmin(String key);
    Set<Tuple> zpopmin(String key, int count);

    /**
     * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。
     * redis> ZRANGE salary 0 -1 WITHSCORES    # 测试数据
     * 1) "jack"
     * 2) "2000"
     * 3) "peter"
     * 4) "3500"
     * 5) "tom"
     * 6) "5000"
     *
     * redis> ZCOUNT salary 2000 5000          # 计算薪水在 2000-5000 之间的人数
     * (integer) 3
     *
     * redis> ZCOUNT salary 3000 5000          # 计算薪水在 3000-5000 之间的人数
     * (integer) 2
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Long zcount(String key, double min, double max);
    Long zcount(String key, String min, String max);

    /**
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     *
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     *
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     *
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。 该选项自 Redis 2.0 版本起可用。
     *
     * 区间及无限
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     *
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     *
     * 举个例子：
     *
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     *
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     *
     * 返回值
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     *
     * 代码示例
     * redis> ZADD salary 2500 jack                        # 测试数据
     * (integer) 0
     * redis> ZADD salary 5000 tom
     * (integer) 0
     * redis> ZADD salary 12000 peter
     * (integer) 0
     *
     * redis> ZRANGEBYSCORE salary -inf +inf               # 显示整个有序集
     * 1) "jack"
     * 2) "tom"
     * 3) "peter"
     *
     * redis> ZRANGEBYSCORE salary -inf +inf WITHSCORES    # 显示整个有序集及成员的 score 值
     * 1) "jack"
     * 2) "2500"
     * 3) "tom"
     * 4) "5000"
     * 5) "peter"
     * 6) "12000"
     *
     * redis> ZRANGEBYSCORE salary -inf 5000 WITHSCORES    # 显示工资 <=5000 的所有成员
     * 1) "jack"
     * 2) "2500"
     * 3) "tom"
     * 4) "5000"
     *
     * redis> ZRANGEBYSCORE salary (5000 400000            # 显示工资大于 5000 小于等于 400000 的成员
     * 1) "peter"
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Set<String> zrangeByScore(String key, double min, double max);
    Set<String> zrangeByScore(String key, String min, String max);
    Set<String> zrangeByScore(String key, double min, double max, int offset, int count);
    Set<String> zrangeByScore(String key, String min, String max, int offset, int count);
    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);
    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);
    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);
    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

    // 同zrangeByScore，只不过zrevrangeByScore按从大到小排序

    Set<String> zrevrangeByScore(String key, double max, double min);
    Set<String> zrevrangeByScore(String key, String max, String min);
    Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);
    Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);
    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);
    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);
    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);
    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

    /**
     * 移除有序集 key 中，指定排名(rank)区间内的所有成员
     *
     * redis> ZADD salary 2000 jack
     * (integer) 1
     * redis> ZADD salary 5000 tom
     * (integer) 1
     * redis> ZADD salary 3500 peter
     * (integer) 1
     *
     * redis> ZREMRANGEBYRANK salary 0 1       # 移除下标 0 至 1 区间内的成员
     * (integer) 2
     *
     * redis> ZRANGE salary 0 -1 WITHSCORES    # 有序集只剩下一个成员
     * 1) "tom"
     * 2) "5000"
     *
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    Long zremrangeByRank(String key, long start, long stop);

    /**
     * 移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员
     *
     * redis> ZRANGE salary 0 -1 WITHSCORES          # 显示有序集内所有成员及其 score 值
     * 1) "tom"
     * 2) "2000"
     * 3) "peter"
     * 4) "3500"
     * 5) "jack"
     * 6) "5000"
     *
     * redis> ZREMRANGEBYSCORE salary 1500 3500      # 移除所有薪水在 1500 到 3500 内的员工
     * (integer) 2
     *
     * redis> ZRANGE salary 0 -1 WITHSCORES          # 剩下的有序集成员
     * 1) "jack"
     * 2) "5000"
     *
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Long zremrangeByScore(String key, double min, double max);
    Long zremrangeByScore(String key, String min, String max);

    /**
     * 对于一个所有成员的分值都相同的有序集合键 key 来说， 这个命令会返回该集合中， 成员介于 min 和 max 范围内的元素数量。
     *
     * 这个命令的 min 参数和 max 参数的意义和 ZRANGEBYLEX key min max [LIMIT offset count] 命令的 min 参数和 max 参数的意义一样。
     *
     * redis> ZADD myzset 0 a 0 b 0 c 0 d 0 e
     * (integer) 5
     *
     * redis> ZADD myzset 0 f 0 g
     * (integer) 2
     *
     * redis> ZLEXCOUNT myzset - +
     * (integer) 7
     *
     * redis> ZLEXCOUNT myzset [b [f
     * (integer) 5
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Long zlexcount(String key, String min, String max);

    /**
     * edis> ZADD myzset 0 a 0 b 0 c 0 d 0 e 0 f 0 g
     * (integer) 7
     *
     * redis> ZRANGEBYLEX myzset - [c
     * 1) "a"
     * 2) "b"
     * 3) "c"
     *
     * redis> ZRANGEBYLEX myzset - (c
     * 1) "a"
     * 2) "b"
     *
     * redis> ZRANGEBYLEX myzset [aaa (g
     * 1) "b"
     * 2) "c"
     * 3) "d"
     * 4) "e"
     * 5) "f"
     *
     * 同zlexcount命令，只不过该命令返回元素，不返回统计的个数
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Set<String> zrangeByLex(String key, String min, String max);
    Set<String> zrangeByLex(String key, String min, String max, int offset, int count);

    /**
     * 127.0.0.1:6379> zrevrangeByLex myzset - (c
     * (empty list or set)
     * 127.0.0.1:6379> zrevrangeByLex myzset  (c -
     * 1) "b"
     * 2) "a"
     *
     * 同zrangeByLex命令，只不过该命令按从大到小排序
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    Set<String> zrevrangeByLex(String key, String max, String min);
    Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);

    /**
     * 对于一个所有成员的分值都相同的有序集合键 key 来说， 这个命令会移除该集合中， 成员介于 min 和 max 范围内的所有元素。
     *
     * redis> ZADD myzset 0 aaaa 0 b 0 c 0 d 0 e
     * (integer) 5
     *
     * redis> ZADD myzset 0 foo 0 zap 0 zip 0 ALPHA 0 alpha
     * (integer) 5
     *
     * redis> ZRANGE myzset 0 -1
     * 1) "ALPHA"
     * 2) "aaaa"
     * 3) "alpha"
     * 4) "b"
     * 5) "c"
     * 6) "d"
     * 7) "e"
     * 8) "foo"
     * 9) "zap"
     * 10) "zip"
     *
     * redis> ZREMRANGEBYLEX myzset [alpha [omega
     * (integer) 6
     *
     * redis> ZRANGE myzset 0 -1
     * 1) "ALPHA"
     * 2) "aaaa"
     * 3) "zap"
     * 4) "zip"
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    Long zremrangeByLex(String key, String min, String max);

    ScanResult<Tuple> zscan(String key, String cursor);
    ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);


    // HyperLogLog：用于统计集合中的元素基数数量

    /**
     * redis> PFADD  databases  "Redis"  "MongoDB"  "MySQL"
     * (integer) 1
     *
     * redis> PFCOUNT  databases
     * (integer) 3
     *
     * redis> PFADD  databases  "Redis"    # Redis 已经存在，不必对估计数量进行更新
     * (integer) 0
     *
     * redis> PFCOUNT  databases    # 元素估计数量没有变化
     * (integer) 3
     *
     * redis> PFADD  databases  "PostgreSQL"    # 添加一个不存在的元素
     * (integer) 1
     *
     * redis> PFCOUNT  databases    # 估计数量增一
     * 4
     *
     * @param key
     * @param elements
     * @return
     */
    Long pfadd(String key, String... elements);
    long pfcount(String key);


    // Geo Commands：GEO特性是Redis 3.2版本的特性，这个功能可以将用户给定的地理位置信息储存起来， 并对这些信息进行操作，使用该命令可以实现“查找附近的人”以及“摇一摇”等功能

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


    // Redis Stream 特性是Redis 5.0之后才有的。Redis Stream的主要应用就是时间序列的消息流分发。PUB/SUB也可以做消息流分发，但是PUB/SUB不记录历史消息，而Redis Stream可以让任何客户端访问任何时刻的数据，并且能记住每一个客户端的访问位置，还能保证消息不丢失。
    // 参考：https://blog.csdn.net/xxywxlyygx/article/details/98382181

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
