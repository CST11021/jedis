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
 * 分片和非分片Jedis的通用接口，分片机制：允许数据存放在不同的机器上，对客户端透明
 */
public interface JedisCommands {


    // Connection 相关命令

    String echo(String string);


    // Key（键）相关命令

    String get(String key);

    Boolean exists(String key);

    String set(String key, String value);

    String set(String key, String value, SetParams params);

    Long del(String key);

    Long unlink(String key);

    /**
     * 移除 key 的过期时间，key 将持久保持
     *
     * @param key
     * @return
     */
    Long persist(String key);

    /**
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
     * restore命令将上面序列化的值进行复原，其中ttl参数代表过期时间，如果ttl=0代表没有过期时间，例如：
     * redis-target> get hello
     * (nil)
     * redis-target> restore hello 0 "\x00\x05world\x06\x00\x8f<T\x04%\xfcNQ"
     * OK
     * redis-target> get hello
     * "world"
     *
     * @param key
     * @param ttl
     * @param serializedValue
     * @return
     */
    String restore(String key, int ttl, byte[] serializedValue);

    String restoreReplace(String key, int ttl, byte[] serializedValue);

    /**
     * 设置过期时间
     *
     * @param key
     * @param seconds
     * @return
     */
    Long expire(String key, int seconds);

    /**
     * pexpire 命令和 expire 命令的作用类似，但是它以毫秒为单位设置 key 的生存时间，而不像 EXPIRE 命令那样，以秒为单位。
     *
     * @param key
     * @param milliseconds
     * @return
     */
    Long pexpire(String key, long milliseconds);

    /**
     * expireat 命令用于以 UNIX 时间戳(unix timestamp)格式设置 key 的过期时间。key 过期后将不再可用
     *
     * @param key
     * @param unixTime
     * @return
     */
    Long expireAt(String key, long unixTime);

    /**
     * Redis PEXPIREAT 命令用于设置 key 的过期时间，以毫秒计。key 过期后将不再可用
     *
     * @param key
     * @param millisecondsTimestamp
     * @return
     */
    Long pexpireAt(String key, long millisecondsTimestamp);

    /**
     * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)
     *
     * @param key
     * @return
     */
    Long ttl(String key);

    /**
     * 以毫秒为单位返回 key 的剩余的过期时间
     *
     * @param key
     * @return
     */
    Long pttl(String key);

    /**
     * 修改某一个或多个key的最后访问时间，如果key不存在，则忽略
     *
     * @param key
     * @return
     */
    Long touch(String key);

    List<String> sort(String key);

    List<String> sort(String key, SortingParams sortingParameters);

    Long move(String key, int dbIndex);


    // String（字符串）

    /**
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
     * 截取字符串
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    String substr(String key, int start, int end);

    Long strlen(String key);

    /**
     * 我们在登陆某些博客网站或者视频网站的时候，网站往往会记录我们是否阅读了某篇文章，或者是观看了某个视频。如果用传统的mysql数据库实现，如果用户数量多，文章和视频也多的情况下，那么则会给数据库带来很大的压力。而用Redis的GETBIT和SETBIT则会简单得多。我们以视频为例，我们用bitmap来记录用户们是否已经观看了某一个视频，一个视频对应一个bitmap。例如
     * <p>
     * key:   video:1201
     * value: 000000...0000
     * key以视频英文名video+冒号+id标记。
     * value就是一个bitmap。一位(bit)有两种可能，0或者1。0代表未看，1代表已经看过了。
     * 而位置(offset)代表的就是user id。例如第200位就代表user_id为200的用户是否观看过id为1201的视频。
     * <p>
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

    Boolean setbit(String key, long offset, String value);

    Boolean getbit(String key, long offset);

    /**
     * 设置或者清空key的value(字符串)在offset处的bit值。
     * <p>
     * 那个位置的bit要么被设置，要么被清空，这个由value（只能是0或者1）来决定。当key不存在的时候，就创建一个新的字符串value。要确保这个字符串大到在offset处有bit值。
     * <p>
     * offset 表示bit的位置数，从0开始计，1字节的bit数组最大为7 。
     *
     * @param key
     * @return
     */
    Long bitcount(String key);

    Long bitcount(String key, long start, long end);

    /**
     * 用指定的字符串覆盖给定 key 所储存的字符串值，覆盖的位置从偏移量 offset 开始：
     * redis 127.0.0.1:6379> SET key1 "Hello World"
     * OK
     * redis 127.0.0.1:6379> SETRANGE key1 6 "Redis"
     * (integer) 11
     * redis 127.0.0.1:6379> GET key1
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
     * Redis Decrby 命令将 key 所储存的值减去指定的减量值。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * <p>
     * # 对已存在的 key 进行 DECRBY
     * <p>
     * redis> SET count 100
     * OK
     * <p>
     * redis> DECRBY count 20
     * (integer) 80
     * <p>
     * <p>
     * # 对不存在的 key 进行DECRBY
     * <p>
     * redis> EXISTS pages
     * (integer) 0
     * <p>
     * redis> DECRBY pages 10
     * (integer) -10
     *
     * @param key
     * @param decrement
     * @return
     */
    Long decrBy(String key, long decrement);

    /**
     * Redis Decr 命令将 key 中储存的数字值减一。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * <p>
     * <p>
     * <p>
     * # 对存在的数字值 key 进行 DECR
     * <p>
     * redis> SET failure_times 10
     * OK
     * <p>
     * redis> DECR failure_times
     * (integer) 9
     * <p>
     * <p>
     * # 对不存在的 key 值进行 DECR
     * <p>
     * redis> EXISTS count
     * (integer) 0
     * <p>
     * redis> DECR count
     * (integer) -1
     * <p>
     * <p>
     * # 对存在但不是数值的 key 进行 DECR
     * <p>
     * redis> SET company YOUR_CODE_SUCKS.LLC
     * OK
     * <p>
     * redis> DECR company
     * (error) ERR value is not an integer or out of range
     *
     * @param key
     * @return
     */
    Long decr(String key);

    Long incrBy(String key, long increment);

    Double incrByFloat(String key, double increment);

    Long incr(String key);

    /**
     * Used for HSTRLEN Redis command
     * Redis Strlen 命令用于获取指定 key 所储存的字符串值的长度。当 key 储存的不是字符串值时，返回一个错误。
     *
     * @param key
     * @param field
     * @return length of the value for key
     */
    Long hstrlen(String key, String field);

    /***
     * 用来获取二进制位串中第一个1或者0的位置
     *
     * @param key
     * @param value
     * @return
     */
    Long bitpos(String key, boolean value);

    Long bitpos(String key, boolean value, BitPosParams params);

    /**
     * Executes BITFIELD Redis command
     *
     * @param key
     * @param arguments
     * @return
     */
    List<Long> bitfield(String key, String... arguments);

    List<Long> bitfieldReadonly(String key, String... arguments);


    // Hash（哈希表）

    /**
     * HSET key field value
     * 将哈希表 key 中的域 field 的值设为 value 。
     * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    Long hset(String key, String field, String value);

    Long hset(String key, Map<String, String> hash);

    String hget(String key, String field);

    Long hsetnx(String key, String field, String value);

    String hmset(String key, Map<String, String> hash);

    List<String> hmget(String key, String... fields);

    Long hincrBy(String key, String field, long value);

    Double hincrByFloat(String key, String field, double value);

    Boolean hexists(String key, String field);

    Long hdel(String key, String... field);

    Long hlen(String key);

    Set<String> hkeys(String key);

    List<String> hvals(String key);

    Map<String, String> hgetAll(String key);

    ScanResult<Map.Entry<String, String>> hscan(String key, String cursor);

    ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params);


    // List（列表）

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
