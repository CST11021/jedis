package redis.clients.jedis.commands;

import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MultiKeyCommands {

    // 操作key的命令

    /**
     * 批量删除键
     *
     * 127.0.0.1:6379> del names testKey1
     * (integer) 2
     *
     *
     * @param keys
     * @return
     */
    Long del(String... keys);
    /**
     * > unlink names testKey1
     *
     * 该命令和DEL十分相似：删除指定的key(s),若key不存在则该key被跳过。但是，相比DEL，del会产生阻塞，而该命令会在另一个线程中回收内存，因此它是非阻塞的。
     * 这也是该命令名字的由来：仅将keys从keyspace元数据中删除，真正的删除会在后续异步操作。
     *
     * @param keys
     * @return
     */
    Long unlink(String... keys);
    /**
     * 127.0.0.1:6379> EXISTS names testKey
     * (integer) 1
     *
     * @param keys
     * @return
     */
    Long exists(String... keys);
    /**
     * 返回存在的key数量
     *
     * redis> SET key1 how
     * OK
     * redis> SET key2 are
     * OK
     * redis> SET key3 you
     * OK
     * redis> TOUCH key1 key2 key3 key4   # key4 不存在
     * (integer) 3
     * redis> TOUCH key5                  # key5 不存在
     * (integer) 0
     *
     *
     * @param keys
     * @return
     */
    Long touch(String... keys);
    /**
     * Returns all the keys matching the glob-style pattern. For example if
     * you have in the database the keys "foo" and "foobar" the command "KEYS foo*" will return
     * "foo foobar".<br>
     * <strong>Warning:</strong> consider this as a command that should be used in production environments with <strong>extreme care</strong>.
     * It may ruin performance when it is executed against large databases.
     * This command is intended for debugging and special operations, such as changing your keyspace layout.
     * <strong>Don't use it in your regular application code.</strong>
     * If you're looking for a way to find keys in a subset of your keyspace, consider using {@link #scan(String, ScanParams)} or sets.
     * <p>
     * While the time complexity for this operation is O(N), the constant times are fairly low.
     * For example, Redis running on an entry level laptop can scan a 1 million key database in 40 milliseconds.
     * <p>
     * Glob style patterns examples:
     * <ul>
     * <li>h?llo will match hello hallo hhllo
     * <li>h*llo will match hllo heeeello
     * <li>h[ae]llo will match hello and hallo, but not hillo
     * </ul>
     * <p>
     * Use \ to escape special chars if you want to match them verbatim.
     * <p>
     * Time complexity: O(n) (with n being the number of keys in the DB, and assuming keys and pattern
     * of limited length)
     *
     * @param pattern
     * @return Multi bulk reply
     * @see <a href="https://redis.io/commands/keys">Redis KEYS documentation</a>
     */
    Set<String> keys(String pattern);
    /**
     * 从redis数据库中随机返回一个key(键名)
     *
     * @return
     */
    String randomKey();

    /**
     * 修改key
     *
     * @param oldkey
     * @param newkey
     * @return
     */
    String rename(String oldkey, String newkey);

    /**
     * 当key存在时，修改key
     *
     * @param oldkey
     * @param newkey
     * @return
     */
    Long renamenx(String oldkey, String newkey);


    // List（列表）

    List<String> blpop(int timeout, String... keys);
    List<String> brpop(int timeout, String... keys);
    List<String> blpop(String... args);
    List<String> brpop(String... args);

    /**
     * 原子性地返回并删除存储在source列表的最后一个元素(尾)，并将该元素推入并存储为destination列表的第一个元素(头)。
     * 如果source不存在，则返回值nil，并且不执行任何操作。如果source和destination相同，则操作等同于从列表中移除最后一个元素，并将其作为列表的第一个元素推入，因此可以将其视为列表回转命令（rotation command）。
     *
     * 127.0.0.1:6379> rpush mylist a b c
     * (integer) 3
     * 127.0.0.1:6379> lrange mylist 0 -1
     * 1) "a"
     * 2) "b"
     * 3) "c"
     * 127.0.0.1:6379> rpoplpush mylist myotherlist
     * "c"
     * 127.0.0.1:6379> lrange mylist 0 -1
     * 1) "a"
     * 2) "b"
     * 127.0.0.1:6379> lrange myotherlist 0 -1
     * 1) "c"
     *
     * @param srckey
     * @param dstkey
     * @return
     */
    String rpoplpush(String srckey, String dstkey);
    String brpoplpush(String source, String destination, int timeout);

    /**
     * 对列表的元素进行排序，并放到目标前key中
     *
     *
     * @param key
     * @param dstkey
     * @return
     */
    Long sort(String key, String dstkey);
    Long sort(String key, SortingParams sortingParameters, String dstkey);

    // 字符串

    /**
     * 批量获取建的值
     *
     * 127.0.0.1:6379> mget whz key_0
     * 1) "\x80"
     * 2) "0"
     *
     * @param keys
     * @return
     */
    List<String> mget(String... keys);
    /**
     * 127.0.0.1:6379> mset key1 111 key2 222
     * OK
     *
     * @param keysvalues
     * @return
     */
    String mset(String... keysvalues);
    /**
     * 在指定的 key 不存在时，为 key 设置指定的值。
     *
     * @param keysvalues
     * @return
     */
    Long msetnx(String... keysvalues);
    /**
     * 在多个键（包含字符串值）之间执行按位操作并将结果存储在目标键中。
     *
     * BITOP 命令支持四个按位运算：AND，OR，XOR和NOT，因此调用该命令的有效形式为：
     *
     * BITOP AND destkey srckey1 srckey2 srckey3 ... srckeyN
     * BITOP OR  destkey srckey1 srckey2 srckey3 ... srckeyN
     * BITOP XOR destkey srckey1 srckey2 srckey3 ... srckeyN
     * BITOP NOT destkey srckey
     *
     *
     *
     * redis> SET键1“foobar”
     * “好”
     * redis> SET key2“abcdef”
     * “好”
     * redis> BITOP和dest key1 key2
     * （整数）6
     * redis> GET dest
     * “`bc`ab”
     *
     *
     * @param op
     * @param destKey
     * @param srcKeys
     * @return
     */
    Long bitop(BitOP op, String destKey, String... srcKeys);



    // Set（集合）

    /**
     * 返回第一个集合与第二个集合的差集, 不存在的集合key将视为空集
     *
     * 127.0.0.1:6379> sadd myset "hello" "foo" "bar"
     * (integer) 3
     * 127.0.0.1:6379> sadd myset2 "hello" "world"
     * (integer) 2
     * 127.0.0.1:6379> sdiff myset myset2
     * 1) "bar"
     * 2) "foo"
     * 127.0.0.1:6379> sdiff myset2 myset
     * 1) "world"
     *
     *
     * @param keys
     * @return
     */
    Set<String> sdiff(String... keys);
    /**
     * 将给定集合之间的差集存储在指定的集合中, 如果指定的集合 key 已存在，则会被覆盖
     *
     * 127.0.0.1:6379> sadd myset "hello" "foo" "bar"
     * (integer) 3
     * 127.0.0.1:6379> sadd myset2 "hello" "world"
     * (integer) 2
     * 127.0.0.1:6379> sdiff myset myset2
     * 1) "bar"
     * 2) "foo"
     * 127.0.0.1:6379> sdiff myset2 myset
     * 1) "world"
     * 127.0.0.1:6379> sdiffstore destset myset myset2
     * (integer) 2
     * 127.0.0.1:6379> smembers destset
     * 1) "bar"
     * 2) "foo"
     * 127.0.0.1:6379> sdiffstore destset myset2 myset
     * (integer) 1
     * 127.0.0.1:6379> smembers destset
     * 1) "world"
     *
     *
     * @param dstkey
     * @param keys
     * @return
     */
    Long sdiffstore(String dstkey, String... keys);

    /**
     * Redis Sinter 命令返回给定所有给定集合的交集。 不存在的集合 key 被视为空集。 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)
     *
     * 127.0.0.1:6379> sadd myset "hello" "foo" "bar"
     * (integer) 3
     * 127.0.0.1:6379> sadd myset2 "hello" "world"
     * (integer) 2
     * 127.0.0.1:6379> sinter myset myset2
     * 1) "hello"
     *
     * @param keys
     * @return
     */
    Set<String> sinter(String... keys);
    /**
     * 127.0.0.1:6379> sadd myset "hello" "foo" "bar"
     * (integer) 3
     * 127.0.0.1:6379> sadd myset2 "hello" "world"
     * (integer) 2
     * 127.0.0.1:6379> sinterstore destset myset myset2
     * (integer) 1
     * 127.0.0.1:6379> smembers destset
     * 1) "hello"
     *
     * @param dstkey
     * @param keys
     * @return
     */
    Long sinterstore(String dstkey, String... keys);

    /**
     * Redis Sunion 命令返回给定集合的并集。不存在的集合 key 被视为空集。
     *
     * redis 127.0.0.1:6379> SADD myset1 "hello"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset1 "world"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset1 "bar"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset2 "hello"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset2 "bar"
     * (integer) 1
     * redis 127.0.0.1:6379> SUNION myset1 myset2
     * 1) "bar"
     * 2) "world"
     * 3) "hello"
     * 4) "foo"
     *
     * @param keys
     * @return
     */
    Set<String> sunion(String... keys);
    /**
     * Redis Sunionstore 命令将给定集合的并集存储在指定的集合 destination 中。
     *
     * redis 127.0.0.1:6379> SADD myset1 "hello"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset1 "world"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset1 "bar"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset2 "hello"
     * (integer) 1
     * redis 127.0.0.1:6379> SADD myset2 "bar"
     * (integer) 1
     * redis 127.0.0.1:6379> SUNIONSTORE myset myset1 myset2
     * (integer) 1
     * redis 127.0.0.1:6379> SMEMBERS myset
     * 1) "bar"
     * 2) "world"
     * 3) "hello"
     * 4) "foo"
     *
     * @param dstkey
     * @param keys
     * @return
     */
    Long sunionstore(String dstkey, String... keys);

    /**
     * Redis Smove 命令将指定成员 member 元素从 source 集合移动到 destination 集合
     *
     * 127.0.0.1:6379> sadd myset "hello" "foo" "bar"
     * (integer) 3
     * 127.0.0.1:6379> sadd myset2 "hello" "world"
     * (integer) 2
     * redis 127.0.0.1:6379> SMOVE myset1 myset2 "bar"
     * (integer) 1
     * redis 127.0.0.1:6379> SMEMBERS myset1
     * 1) "World"
     * 2) "Hello"
     * redis 127.0.0.1:6379> SMEMBERS myset2
     * 1) "foo"
     * 2) "bar"
     *
     * @param srckey
     * @param dstkey
     * @param member
     * @return
     */
    Long smove(String srckey, String dstkey, String member);

    // 有序结合

    /**
     * 取有序集合的交集，并存储到dstkey键中
     *
     * redis > ZADD mid_test 70 "Li Lei"
     * (integer) 1
     * redis > ZADD mid_test 70 "Han Meimei"
     * (integer) 1
     * redis > ZADD mid_test 99.5 "Tom"
     * (integer) 1
     *
     * redis > ZADD fin_test 88 "Li Lei"
     * (integer) 1
     * redis > ZADD fin_test 75 "Han Meimei"
     * (integer) 1
     * redis > ZADD fin_test 99.5 "Tom"
     * (integer) 1
     *
     * redis > ZINTERSTORE sum_point 2 mid_test fin_test
     * (integer) 3
     *
     * redis > ZRANGE sum_point 0 -1 WITHSCORES     # 显示有序集内所有成员及其 score 值
     * 1) "Han Meimei"
     * 2) "145"
     * 3) "Li Lei"
     * 4) "158"
     * 5) "Tom"
     * 6) "199"
     *
     *
     * @param dstkey
     * @param sets
     * @return
     */
    Long zinterstore(String dstkey, String... sets);
    Long zinterstore(String dstkey, ZParams params, String... sets);

    /**
     * 取有序集合的并集，并存储到dstkey键中
     * redis> ZRANGE programmer 0 -1 WITHSCORES
     * 1) "peter"
     * 2) "2000"
     * 3) "jack"
     * 4) "3500"
     * 5) "tom"
     * 6) "5000"
     *
     * redis> ZRANGE manager 0 -1 WITHSCORES
     * 1) "herry"
     * 2) "2000"
     * 3) "mary"
     * 4) "3500"
     * 5) "bob"
     * 6) "4000"
     *
     * redis> ZUNIONSTORE salary 2 programmer manager WEIGHTS 1 3   # 公司决定加薪。。。除了程序员。。。
     * (integer) 6
     *
     * redis> ZRANGE salary 0 -1 WITHSCORES
     * 1) "peter"
     * 2) "2000"
     * 3) "jack"
     * 4) "3500"
     * 5) "tom"
     * 6) "5000"
     * 7) "herry"
     * 8) "6000"
     * 9) "mary"
     * 10) "10500"
     * 11) "bob"
     * 12) "12000"
     *
     *
     * @param dstkey
     * @param sets
     * @return
     */
    Long zunionstore(String dstkey, String... sets);
    Long zunionstore(String dstkey, ZParams params, String... sets);




    /**
     * @param cursor
     * @return
     * @see #scan(String, ScanParams)
     */
    ScanResult<String> scan(String cursor);
    /**
     * Iterates the set of keys in the currently selected Redis database.
     * <p>
     * Since this command allows for incremental iteration, returning only a small number of elements per call,
     * it can be used in production without the downside of commands like {@link #keys(String)} or
     * {@link JedisCommands#smembers(String)} )} that may block the server for a long time (even several seconds)
     * when called against big collections of keys or elements.
     * <p>
     * SCAN basic usage<br>
     * SCAN is a cursor based iterator. This means that at every call of the command, the server returns an updated cursor
     * that the user needs to use as the cursor argument in the next call.
     * An iteration starts when the cursor is set to 0, and terminates when the cursor returned by the server is 0.
     * <p>
     * Scan guarantees<br>
     * The SCAN command, and the other commands in the SCAN family, are able to provide to the user a set of guarantees
     * associated to full iterations.
     * <ul>
     * <li>A full iteration always retrieves all the elements that were present in the collection from the start to the
     * end of a full iteration. This means that if a given element is inside the collection when an iteration is started,
     * and is still there when an iteration terminates, then at some point SCAN returned it to the user.
     * <li>A full iteration never returns any element that was NOT present in the collection from the start to the end of
     * a full iteration. So if an element was removed before the start of an iteration, and is never added back to the
     * collection for all the time an iteration lasts, SCAN ensures that this element will never be returned.
     * </ul>
     * However because SCAN has very little state associated (just the cursor) it has the following drawbacks:
     * <ul>
     * <li>A given element may be returned multiple times. It is up to the application to handle the case of duplicated
     * elements, for example only using the returned elements in order to perform operations that are safe when re-applied
     * multiple times.
     * <li>Elements that were not constantly present in the collection during a full iteration, may be returned or not:
     * it is undefined.
     * </ul>
     * <p>
     * Time complexity: O(1) for every call. O(N) for a complete iteration, including enough command calls for the cursor
     * to return back to 0. N is the number of elements inside the DB.
     *
     * @param cursor The cursor.
     * @param params the scan parameters. For example a glob-style match pattern
     * @return the scan result with the results of this iteration and the new position of the cursor
     * @see <a href="https://redis.io/commands/scan">Redis SCAN documentation</a>
     */
    ScanResult<String> scan(String cursor, ScanParams params);



    // Redis Streams

    /**
     * XREAD [COUNT count] [BLOCK milliseconds] STREAMS key [key ...] ID [ID ...]
     *
     * @param count
     * @param block
     * @param streams
     * @return
     */
    List<Map.Entry<String, List<StreamEntry>>> xread(int count, long block, Map.Entry<String, StreamEntryID>... streams);
    /**
     * XREAD [COUNT count] [BLOCK milliseconds] STREAMS key [key ...] ID [ID ...]
     *
     * @param groupname
     * @param consumer
     * @param count
     * @param block
     * @param streams
     * @return
     */
    List<Map.Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, int count, long block, final boolean noAck, Map.Entry<String, StreamEntryID>... streams);


    // HyperLogLog

    /**
     * 当 PFCOUNT key [key …] 命令作用于单个键时， 返回储存在给定键的 HyperLogLog 的近似基数， 如果键不存在， 那么返回 0 。
     *
     * 当 PFCOUNT key [key …] 命令作用于多个键时， 返回所有给定 HyperLogLog 的并集的近似基数， 这个近似基数是通过将所有给定 HyperLogLog 合并至一个临时 HyperLogLog 来计算得出的。
     *
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
     *
     * @param keys
     * @return
     */
    long pfcount(String... keys);
    /**
     * 时间复杂度： O(N) ， 其中 N 为被合并的 HyperLogLog 数量， 不过这个命令的常数复杂度比较高。
     * 将多个 HyperLogLog 合并（merge）为一个 HyperLogLog ， 合并后的 HyperLogLog 的基数接近于所有输入 HyperLogLog 的可见集合（observed set）的并集。
     *
     * 合并得出的 HyperLogLog 会被储存在 destkey 键里面， 如果该键并不存在， 那么命令在执行之前， 会先为该键创建一个空的 HyperLogLog 。
     *
     *
     * redis> PFADD  nosql  "Redis"  "MongoDB"  "Memcached"
     * (integer) 1
     *
     * redis> PFADD  RDBMS  "MySQL" "MSSQL" "PostgreSQL"
     * (integer) 1
     *
     * redis> PFMERGE  databases  nosql  RDBMS
     * OK
     *
     * redis> PFCOUNT  databases
     * (integer) 6
     *
     * @param destkey
     * @param sourcekeys
     * @return
     */
    String pfmerge(String destkey, String... sourcekeys);

    // 发布订阅相关接口

    Long publish(String channel, String message);
    void subscribe(JedisPubSub jedisPubSub, String... channels);
    /**
     * 订阅指定模式匹配的频道
     *
     * @param jedisPubSub
     * @param patterns
     */
    void psubscribe(JedisPubSub jedisPubSub, String... patterns);


    // redis事务相关

    /**
     * 监视一个(或多个) key ，如果在事务执行之前这个(或这些) key 被其他命令所改动，那么事务将被打断。
     *
     * redis> WATCH lock lock_times
     * OK
     *
     * @param keys
     * @return
     */
    String watch(String... keys);
    String unwatch();
}
