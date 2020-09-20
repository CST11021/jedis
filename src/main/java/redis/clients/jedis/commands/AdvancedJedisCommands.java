package redis.clients.jedis.commands;

import redis.clients.jedis.AccessControlUser;
import redis.clients.jedis.params.ClientKillParams;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.util.Slowlog;

import java.util.List;

public interface AdvancedJedisCommands {

    /**
     * 获取redis服务配置：
     * 127.0.0.1:6379> CONFIG GET port
     * 1) "port"
     * 2) "6379"
     * 127.0.0.1:6379> CONFIG GET bind
     * 1) "bind"
     * 2) ""
     * 127.0.0.1:6379> CONFIG GET tcp-keepalive
     * 1) "tcp-keepalive"
     * 2) "300"
     *
     * CONFIG GET * 命令可以获取全部配置
     *
     * @param pattern
     * @return
     */
    List<String> configGet(String pattern);

    /**
     * 所有被 CONFIG SET 修改的配置参数都会立即生效
     *
     * 127.0.0.1:6379> CONFIG GET slowlog-max-len
     * 1) "slowlog-max-len"
     * 2) "128"
     * 127.0.0.1:6379> CONFIG SET slowlog-max-len 10010
     * OK
     * 127.0.0.1:6379> CONFIG GET slowlog-max-len
     * 1) "slowlog-max-len"
     * 2) "10010"
     *
     *
     * @param parameter
     * @param value
     * @return
     */
    String configSet(String parameter, String value);

    /**
     * Slow log 的行为由两个配置参数(configuration parameter)指定，可以通过改写 redis.conf 文件或者用 CONFIG GET 和 CONFIG SET 命令对它们动态地进行修改。
     *
     * 第一个选项是 slowlog-log-slower-than ，它决定要对执行时间大于多少微秒(microsecond，1秒 = 1,000,000 微秒)的查询进行记录。
     *
     * 比如执行以下命令将让 slow log 记录所有查询时间大于等于 100 微秒的查询：
     *
     * CONFIG SET slowlog-log-slower-than 100
     *
     * 而以下命令记录所有查询时间大于 1000 微秒的查询：
     *
     * CONFIG SET slowlog-log-slower-than 1000
     *
     * 另一个选项是 slowlog-max-len ，它决定 slow log 最多能保存多少条日志， slow log 本身是一个 FIFO 队列，当队列大小超过 slowlog-max-len 时，最旧的一条日志将被删除，而最新的一条日志加入到 slow log ，以此类推。
     *
     * 以下命令让 slow log 最多保存 1000 条日志：
     *
     * CONFIG SET slowlog-max-len 1000
     *
     * @return
     */
    String slowlogReset();
    Long slowlogLen();
    List<Slowlog> slowlogGet();
    List<Slowlog> slowlogGet(long entries);

    /**
     * OBJECT 命令有多个子命令：
     *
     * OBJECT REFCOUNT <key> 返回给定 key 该命令主要用于调试(debugging)，它能够返回指定key所对应value被引用的次数.
     * OBJECT ENCODING <key> 返回给定 key 储存的值所使用的内部表示(representation)。
     * OBJECT IDLETIME <key> 返回给定 key 自储存以来的空转时间(idle， 没有被读取也没有被写入)，以秒为单位。
     *
     * redis> SET game "COD"           # 设置一个字符串
     * OK
     *
     * redis> OBJECT REFCOUNT game     # 只有一个引用
     * (integer) 1
     *
     * redis> OBJECT IDLETIME game     # 等待一阵。。。然后查看空转时间
     * (integer) 90
     *
     * redis> GET game                 # 提取game， 让它处于活跃(active)状态
     * "COD"
     *
     * redis> OBJECT IDLETIME game     # 不再处于空转
     * (integer) 0
     *
     * redis> OBJECT ENCODING game     # 字符串的编码方式
     * "raw"
     *
     * redis> SET phone 15820123123    # 大的数字也被编码为字符串
     * OK
     *
     * redis> OBJECT ENCODING phone
     * "raw"
     *
     * redis> SET age 20               # 短数字被编码为 int
     * OK
     *
     * redis> OBJECT ENCODING age
     * "int"
     *
     * @param key
     * @return
     */
    Long objectRefcount(String key);
    String objectEncoding(String key);
    Long objectIdletime(String key);
    List<String> objectHelp();
    Long objectFreq(String key);

    /**
     * 将 key 原子性地从当前实例传送到目标实例的指定数据库上，一旦传送成功， key 保证会出现在目标实例上，而当前实例上的 key 会被删除。
     *
     * 这个命令是一个原子操作，它在执行的时候会阻塞进行迁移的两个实例，直到以下任意结果发生：迁移成功，迁移失败，等到超时。
     *
     * 命令的内部实现是这样的：它在当前实例对给定 key 执行 DUMP 命令 ，将它序列化，然后传送到目标实例，目标实例再使用 RESTORE 对数据进行反序列化，并将反序列化所得的数据添加到数据库中；当前实例就像目标实例的客户端那样，只要看到 RESTORE 命令返回 OK ，它就会调用 DEL 删除自己数据库上的 key 。
     *
     * timeout 参数以毫秒为格式，指定当前实例和目标实例进行沟通的最大间隔时间。这说明操作并不一定要在 timeout 毫秒内完成，只是说数据传送的时间不能超过这个 timeout 数。
     *
     * MIGRATE 命令需要在给定的时间规定内完成 IO 操作。如果在传送数据时发生 IO 错误，或者达到了超时时间，那么命令会停止执行，并返回一个特殊的错误： IOERR 。
     *
     * 当 IOERR 出现时，有以下两种可能：
     *
     * key 可能存在于两个实例
     * key 可能只存在于当前实例
     * 唯一不可能发生的情况就是丢失 key ，因此，如果一个客户端执行 MIGRATE 命令，并且不幸遇上 IOERR 错误，那么这个客户端唯一要做的就是检查自己数据库上的 key 是否已经被正确地删除。
     *
     * 如果有其他错误发生，那么 MIGRATE 保证 key 只会出现在当前实例中。（当然，目标实例的给定数据库上可能有和 key 同名的键，不过这和 MIGRATE 命令没有关系）。
     *
     * @param host
     * @param port
     * @param key
     * @param destinationDB
     * @param timeout
     * @return
     */
    String migrate(String host, int port, String key, int destinationDB, int timeout);
    String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys);

    /**
     * # 列出所有已连接客户端
     *
     * redis 127.0.0.1:6379> CLIENT LIST
     * addr=127.0.0.1:43501 fd=5 age=10 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=client
     *
     * # 杀死当前客户端的连接
     *
     * redis 127.0.0.1:6379> CLIENT KILL 127.0.0.1:43501
     * OK
     *
     * # 之前的连接已经被关闭，CLI 客户端又重新建立了连接
     * # 之前的端口是 43501 ，现在是 43504
     *
     * redis 127.0.0.1:6379> CLIENT LIST
     * addr=127.0.0.1:43504 fd=5 age=0 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=client
     *
     * @param ipPort
     * @return
     */
    String clientKill(String ipPort);
    String clientKill(String ip, int port);
    Long clientKill(ClientKillParams params);
    String clientGetname();
    String clientList();
    String clientSetname(String name);



    String memoryDoctor();

    String aclWhoAmI();

    String aclGenPass();

    List<String> aclList();

    List<String> aclUsers();

    AccessControlUser aclGetUser(String name);

    String aclSetUser(String name);

    String aclSetUser(String name, String... keys);

    Long aclDelUser(String name);

    List<String> aclCat();

    List<String> aclCat(String category);

    // TODO: Implements ACL LOAD/SAVE commands
}
