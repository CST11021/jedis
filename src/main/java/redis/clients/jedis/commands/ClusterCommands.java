package redis.clients.jedis.commands;

import redis.clients.jedis.ClusterReset;

import java.util.List;

public interface ClusterCommands {

    /**
     * 查看集群节点信息：
     * 其中返回值第一项表示节点id，由40个16进制字符串组成，节点id与主从复制一文中提到的runId不同：Redis每次启动runId都会重新创建，
     * 但是节点id只在集群初始化时创建一次，然后保存到集群配置文件中，以后节点重新启动时会直接在集群配置文件中读取。
     *
     * 需要特别注意，在启动节点阶段，节点是没有主从关系的，因此从节点不需要加slaveof配置。
     *
     * 172.16.120.253:7001> cluster nodes
     * 627796aa5e3ba1f9051b1a91089f72c356ef13b4 172.16.120.253:7002@17002 master - 0 1600306235927 3 connected 10923-16383
     * 42ac01898fa96541f3bbe7994e8d02cbb8cce390 172.16.120.253:7000@17000 master - 0 1600306234896 1 connected 0-5460
     * 7a7eb1ee328431feceda022e9331fb6aa65e6e3e 172.16.120.253:7001@17001 myself,master - 0 1600306235000 2 connected 5461-10922
     *
     * @return
     */
    String clusterNodes();

    /**
     * 节点握手:
     * 节点启动以后是相互独立的，并不知道其他节点存在；需要进行节点握手，将独立的节点组成一个网络,节点握手使用cluster meet {ip} {port}命令实现，
     * 例如，在7000节点中执行cluster meet 192.168.72.128 7001，可以完成7000节点和7001节点的握手；
     * 注意，ip使用的是局域网ip，而不是localhost或127.0.0.1，是为了其他机器上的节点或客户端也可以访问。
     *
     * > cluster meet 192.168.72.128 7001
     *
     * @param ip
     * @param port
     * @return
     */
    String clusterMeet(String ip, int port);

    /**
     * 分配槽：
     *
     * 在Redis集群中，借助槽实现数据分区，集群有16384个槽，槽是数据管理和迁移的基本单位。当数据库中的16384个槽都分配了节点时，集群处于上线状态（ok）；
     * 如果有任意一个槽没有分配节点，则集群处于下线状态（fail）。cluster info命令可以查看集群状态，分配槽之前状态为fail：
     *
     *
     * 分配槽使用cluster addslots命令，执行下面的命令将槽（编号0-16383）全部分配完毕：
     *
     * redis-cli -p 7000> cluster addslots {0..5461}
     * redis-cli -p 7001> cluster addslots {5462..10922}
     * redis-cli -p 7002> cluster addslots {10923..16383}
     * 此时查看集群状态，显示所有槽分配完毕，集群进入上线状态：
     *
     * @param slots
     * @return
     */
    String clusterAddSlots(int... slots);

    /**
     * 删除redis分配的槽
     *
     * @param slots
     * @return
     */
    String clusterDelSlots(int... slots);

    /**
     * 查看集群信息：
     * 172.16.120.253:7001> cluster info
     * cluster_state:ok
     * cluster_slots_assigned:16384
     * cluster_slots_ok:16384
     * cluster_slots_pfail:0
     * cluster_slots_fail:0
     * cluster_known_nodes:3
     * cluster_size:3
     * cluster_current_epoch:3
     * cluster_my_epoch:2
     * cluster_stats_messages_ping_sent:34117
     * cluster_stats_messages_pong_sent:34493
     * cluster_stats_messages_meet_sent:2
     * cluster_stats_messages_sent:68612
     * cluster_stats_messages_ping_received:34493
     * cluster_stats_messages_pong_received:34117
     * cluster_stats_messages_fail_received:2
     * cluster_stats_messages_received:68612
     *
     * @return
     */
    String clusterInfo();

    /**
     *
     *
     * 该命令返回存储在联系节点中的密钥名称数组，并哈希到指定的哈希槽。通过count参数指定要返回的最大键数，以便 该 API 的用户可以批量处理键。
     * 此命令的主要用途是在将集群插槽从一个节点重新组合到另一个节点期间。重做哈希的方式在 Redis 集群规范中公开，或者以更简单的摘要形式公开，作为CLUSTER SETSLOT 命令文档的附录。
     * 时间复杂度： O（log（N））其中 N 是请求的键的数量：
     * > CLUSTER GETKEYSINSLOT 7000 3
     * "47344|273766|70329104160040|key_39015"
     * "47344|273766|70329104160040|key_89793"
     * "47344|273766|70329104160040|key_92937"
     *
     * @param slot
     * @param count
     * @return
     */
    List<String> clusterGetKeysInSlot(int slot, int count);


    String clusterSetSlotNode(int slot, String nodeId);
    String clusterSetSlotMigrating(int slot, String nodeId);
    String clusterSetSlotImporting(int slot, String nodeId);
    String clusterSetSlotStable(int slot);

    String clusterForget(String nodeId);

    String clusterFlushSlots();

    /**
     * 获取key所在的槽索引，该命令主要用来调试和测试，因为它通过一个API来暴露Redis底层哈希算法的实现。
     * 172.16.120.253:7001> CLUSTER KEYSLOT name
     * (integer) 5798
     *
     *
     * @param key
     * @return
     */
    Long clusterKeySlot(String key);

    /**
     * 返回连接节点负责的指定hash slot的key的数量。该命令只查询连接节点的数据集，所以如果连接节点指派到该hash slot会返回0。
     *
     * > CLUSTER COUNTKEYSINSLOT 7000
     * (integer) 0
     * 172.16.120.253:7001> CLUSTER COUNTKEYSINSLOT 5798
     * (integer) 1
     *
     * @param slot
     * @return
     */
    Long clusterCountKeysInSlot(int slot);

    /**
     * 强制保存配置nodes.conf至磁盘，该命令主要用于nodes.conf节点状态文件丢失或被删除的情况下重新生成文件。当使用CLUSTER命令对群集做日常维护时，
     * 该命令可以用于保证新生成的配置信息会被持久化到磁盘。当然，这类命令应该没定时调用 将配置信息持久化到磁盘，保证系统重启之后状态信息还是正确的。
     *
     * @return
     */
    String clusterSaveConfig();

    /**
     * 该命令重新配置一个节点成为指定master的salve节点。 如果收到命令的节点是一个empty master，那么该节点的角色将由master切换为slave。
     *
     * 一旦一个节点变成另一个master节点的slave，无需通知群集内其他节点这一变化：节点间交换 信息的心跳包会自动将新的配置信息分发至所有节点。
     *
     * 基于如下假设，一个slave节点会接受该命令
     *
     * 指定节点在它的节点信息表中存在
     *
     * 指定节点无法识别接收我们命令的节点实例
     *
     * 指定节点是一个master
     *
     * 如果收到命令的节点不是slave而是master，只要在如下情况下，命令才会执行成功，该节点才会切换为slave：
     *
     * 该节点不保存任何hash槽
     *
     * 该节点是空的，key空间中不存储任何键
     *
     * 如果命令执行成功，新的slave会立即尝试连接它的master以便进行数据复制
     *
     * @param nodeId
     * @return
     */
    String clusterReplicate(String nodeId);

    /**
     * 该命令会列出指定master节点所有slave节点，格式同CLUSTER NODES(详见指定格式说明文档)
     *
     * 当指定节点未知或者根据接收命令的节点的节点信息表指定节点不是主节点，命令执行错误。
     *
     * 注意：当一个slave被添加，移动或者删除时，我们在一个配置信息没有更新的群集节点上执行命令CLUSTER SLAVES获取将是脏信息。 不过最终(无网络分区的情况下大概几秒钟)所有节点都会同步指定master节点的salve节点信息。
     *
     * @param nodeId
     * @return
     */
    List<String> clusterSlaves(String nodeId);

    /**
     * 该命令只能在群集slave节点执行，让slave节点进行一次人工故障切换。
     *
     * 人工故障切换是预期的操作，而非发生了真正的故障，目的是以一种安全的方式(数据无丢失)将当前master节点和其中一个slave节点(执行cluster-failover的节点)交换角色。 流程如下：
     *
     * 当前slave节点告知其master节点停止处理来自客户端的请求
     *
     * master 节点将当前replication offset 回复给该slave节点
     *
     * 该slave节点在未应用至replication offset之前不做任何操作，以保证master传来的数据均被处理。
     *
     * 该slave 节点进行故障转移，从群集中大多数的master节点获取epoch，然后广播自己的最新配置
     *
     * 原master节点收到配置更新:解除客户端的访问阻塞，回复重定向信息，以便客户端可以和新master通信。
     *
     * 当该slave节点(将切换为新master节点)处理完来自master的所有
     *
     * @return
     */
    String clusterFailover();

    /**
     * CLUSTER SLOTS命令返回哈希槽和Redis实例映射关系。这个命令对客户端实现集群功能非常有用，使用这个命令可以获得哈希槽与节点（由IP和端口组成）的映射关系，这样，当客户端收到（用户的）调用命令时，可以根据（这个命令）返回的信息将命令发送到正确的Redis实例.
     *
     * 172.16.120.253:7001> CLUSTER SLOTS
     * 1) 1) (integer) 10923
     *    2) (integer) 16383
     *    3) 1) "172.16.120.253"
     *       2) (integer) 7002
     *       3) "627796aa5e3ba1f9051b1a91089f72c356ef13b4"
     * 2) 1) (integer) 0
     *    2) (integer) 5460
     *    3) 1) "172.16.120.253"
     *       2) (integer) 7000
     *       3) "42ac01898fa96541f3bbe7994e8d02cbb8cce390"
     * 3) 1) (integer) 5461
     *    2) (integer) 10922
     *    3) 1) "172.16.120.253"
     *       2) (integer) 7001
     *       3) "7a7eb1ee328431feceda022e9331fb6aa65e6e3e"
     *
     * @return
     */
    List<Object> clusterSlots();

    /**
     * 根据reset的类型配置hard或者soft ，Reset 一个Redis群集节点可以选择十分极端或极端的方式。 注意该命令在主节点hold住一个或多个keys的时候无效，在这种情况下，如果要彻底reset一个master， 需要将它的所有key先移除，如先使用FLUSHALL，在使用CLUSTER RESET
     *
     * 节点上的效果如下：
     *
     * 群集中的节点都被忽略
     *
     * 所有已分派/打开的槽会被reset，以便slots-to-nodes对应关系被完全清除
     *
     * 如果节点是slave，它会被切换为(空)master。它的数据集已被清空，因此最后也会变成一个空master。
     *
     * **Hard reset only：生成新的节点ID
     *
     * Hard reset only：变量currentEpoch 和configEpoch被设置为0
     *
     * 新配置被持久化到节点磁盘上的群集配置信息文件中
     *
     * 当需要为一个新的或不同的群集提供一个新的群集节点是可使用该命令，同时它也在Redis群集测试框架中被广泛使用，它用于 在每个新的测试单元启动是初始化群集状态。
     *
     * 如果reset类型没有指定，使用默认值soft
     *
     * @param resetType
     * @return
     */
    String clusterReset(ClusterReset resetType);

    String readonly();
}
