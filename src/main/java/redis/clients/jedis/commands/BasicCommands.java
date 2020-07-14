package redis.clients.jedis.commands;

import redis.clients.jedis.DebugParams;

public interface BasicCommands {

    /**
     * 该命令通常用于测试连接是否仍处于活动状态或测量延迟
     *
     * @return PONG
     */
    String ping();

    /**
     * 在受密码保护的Redis服务器中请求身份验证。
     * 可以指示Redis在允许客户端执行命令之前要求输入密码。
     * 这是使用配置文件中的requirepass指令完成的。
     * 如果password与配置文件中的密码匹配，则服务器将以OK状态代码进行答复并开始接受命令。
     * Otherwise, an error is returned and the clients needs to try a new password.
     *
     * @param password
     * @return the result of the auth
     */
    String auth(String password);

    /**
     * Request for authentication with username and password, based on the  ACL feature introduced in Redis 6.0
     * see https://redis.io/topics/acl
     *
     * @param user
     * @param password
     * @return
     */
    String auth(String user, String password);

    /**
     * Ask the server to close the connection. The connection is closed as soon as all pending replies have been written to the client.
     *
     * @return OK
     */
    String quit();


    /**
     * 删除当前所选数据库的所有键, 此命令永远不会失败, 此操作的时间复杂度为O(N), N为数据库中键的数量
     *
     * @return OK
     */
    String flushDB();

    /**
     * 删除所有现有数据库的所有键，而不仅仅是当前选择的一个
     *
     * @return a simple string reply (OK)
     */
    String flushAll();

    /**
     * 返回当前所选数据库中的键数。
     *
     * @return the number of key in the currently-selected database.
     */
    Long dbSize();

    /**
     * 选择具有指定的从零开始的数字索引的DB。
     *
     * @param index the index
     * @return a simple string reply OK
     */
    String select(int index);

    /**
     * 此命令交换两个Redis数据库，以便立即连接到给定数据库的所有客户端将看到另一个数据库的数据，反之亦然。
     *
     * @param index1
     * @param index2
     * @return Simple string reply: OK if SWAPDB was executed correctly.
     */
    String swapDB(int index1, int index2);


    /**
     * SAVE命令执行数据集的同步保存，以RDB文件的形式生成Redis实例内所有数据的时间点快照。
     * 您几乎从不希望在生产环境中调用SAVE，因为它将阻止所有其他客户端，相反，通常使用BGSAVE。
     * 但是，如果遇到阻止Redis创建后台保存子项的问题（例如fork（2）系统调用中的错误），SAVE命令可能是执行最新数据集转储的最后选择。
     *
     * @return result of the save
     */
    String save();

    /**
     * 将数据库保存在后台。 The OK code is immediately returned. Redis forks, the parent continues to serve the clients,
     * the child saves the DB on disk then exits. A client may be able to check if the operation succeeded using the LASTSAVE command.
     *
     * @return ok
     */
    String bgsave();

    /**
     * Instruct Redis to start an Append Only File rewrite process. The rewrite will create a small optimized version of the current Append Only File
     * If BGREWRITEAOF fails, no data gets lost as the old AOF will be untouched.
     * The rewrite will be only triggered by Redis if there is not already a background process doing persistence. Specifically:
     * If a Redis child is creating a snapshot on disk, the AOF rewrite is scheduled but not started until the saving child producing the RDB file terminates. In this case the BGREWRITEAOF will still return an OK code, but with an appropriate message. You can check if an AOF rewrite is scheduled looking at the INFO command as of Redis 2.6.
     * If an AOF rewrite is already in progress the command returns an error and no AOF rewrite will be scheduled for a later time.
     * Since Redis 2.4 the AOF rewrite is automatically triggered by Redis, however the BGREWRITEAOF command can be used to trigger a rewrite at any time.
     *
     * @return the response of the command
     */
    String bgrewriteaof();

    /**
     * 返回成功执行的最后一个数据库保存的 UNIX TIME
     *
     * @return the unix latest save
     */
    Long lastsave();

    /**
     * 停止所有客户端。执行保存（如果配置了一个保存点），如果启用了AOF，则刷新仅附加文件退出服务器
     *
     * @return only in case of error.
     */
    String shutdown();

    /**
     * INFO命令以一种易于计算机解析和易于阅读的格式返回有关服务器的信息和统计信息。
     *
     * @return information on the server
     */
    String info();

    /**
     * INFO命令以一种易于计算机解析和易于阅读的格式返回有关服务器的信息和统计信息。
     *
     * @param section (all: Return all sections, default: Return only the default set of sections, server: General information about the Redis server, clients: Client connections section, memory: Memory consumption related information, persistence: RDB and AOF related information, stats: General statistics, replication: Master/slave replication information, cpu: CPU consumption statistics, commandstats: Redis command statistics, cluster: Redis Cluster section, keyspace: Database related statistics)
     * @return
     */
    String info(String section);

    /**
     * The SLAVEOF command can change the replication settings of a slave on the fly. In the proper form SLAVEOF hostname port will make the server a slave of another server listening at the specified hostname and port.
     * If a server is already a slave of some master, SLAVEOF hostname port will stop the replication against the old server and start the synchronization against the new one, discarding the old dataset.
     *
     * @param host listening at the specified hostname
     * @param port server listening at the specified port
     * @return result of the command.
     */
    String slaveof(String host, int port);

    /**
     * SLAVEOF NO ONE will stop replication, turning the server into a MASTER, but will not discard the replication. So, if the old master stops working, it is possible to turn the slave into a master and set the application to use this new master in read/write. Later when the other Redis server is fixed, it can be reconfigured to work as a slave.
     *
     * @return result of the command
     */
    String slaveofNoOne();

    /**
     * 返回当前数据库的索引
     *
     * @return the int of the index database.
     */
    int getDB();

    String debug(DebugParams params);

    String configResetStat();

    String configRewrite();

    /**
     * 阻塞，直到至少所有指定数量的副本成功传输并确认所有先前的写命令为止。
     * 如果达到了以毫秒为单位指定的超时时间，即使尚未达到指定数量的副本，命令也会返回。
     *
     * @param replicas 成功转移并确认至少指定数量的副本
     * @param timeout  阻塞的时间（以毫秒为单位），超时值为0表示永远阻塞
     * @return 在当前连接的上下文中执行的所有写入所达到的副本数
     */
    Long waitReplicas(int replicas, long timeout);
}
