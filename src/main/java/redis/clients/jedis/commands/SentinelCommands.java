package redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;

/**
 * 哨兵相关命令：
 *
 * Redis 的 Sentinel 系统用于管理多个 Redis 服务器（instance）， 该系统执行以下三个任务：
 *
 * 监控（Monitoring）： Sentinel 会不断地检查你的主服务器和从服务器是否运作正常。
 * 提醒（Notification）： 当被监控的某个 Redis 服务器出现问题时， Sentinel 可以通过 API 向管理员或者其他应用程序发送通知。
 * 自动故障迁移（Automatic failover）： 当一个主服务器不能正常工作时， Sentinel 会开始一次自动故障迁移操作， 它会将失效主服务器的其中一个从服务器升级为新的主服务器， 并让失效主服务器的其他从服务器改为复制新的主服务器； 当客户端试图连接失效的主服务器时， 集群也会向客户端返回新主服务器的地址， 使得集群可以使用新主服务器代替失效服务器。
 *
 * Redis Sentinel 是一个分布式系统，你可以在一个架构中运行多个 Sentinel 进程（progress），这些进程使用流言协议（gossip protocols)来
 * 接收关于主服务器是否下线的信息，并使用投票协议（agreement protocols）来决定是否执行自动故障迁移， 以及选择哪个从服务器作为新的主服务器。
 *
 */
public interface SentinelCommands {

    /**
     * SENTINEL masters ：列出所有被监视的主服务器，以及这些主服务器的当前状态。
     *
     * 127.0.0.1:26380> sentinel masters
     * 1)  1) "name"
     *     2) "myMaster"
     *     3) "ip"
     *     4) "127.0.0.1"
     *     5) "port"
     *     6) "6381"
     *     7) "runid"
     *     8) "68a74d3a6125186dbd9d3ad9c3f62414a7bb0bd7"
     *     9) "flags"
     *    10) "master"
     *    11) "link-pending-commands"
     *    12) "0"
     *    13) "link-refcount"
     *    14) "1"
     *    15) "last-ping-sent"
     *    16) "0"
     *    17) "last-ok-ping-reply"
     *    18) "602"
     *    19) "last-ping-reply"
     *    20) "602"
     *    21) "down-after-milliseconds"
     *    22) "30000"
     *    23) "info-refresh"
     *    24) "4649"
     *    25) "role-reported"
     *    26) "master"
     *    27) "role-reported-time"
     *    28) "54651337"
     *    29) "config-epoch"
     *    30) "1"
     *    31) "num-slaves"
     *    32) "2"
     *    33) "num-other-sentinels"
     *    34) "0"
     *    35) "quorum"
     *    36) "1"
     *    37) "failover-timeout"
     *    38) "180000"
     *    39) "parallel-syncs"
     *    40) "1"
     *
     * @return
     */
    List<Map<String, String>> sentinelMasters();

    /**
     * SENTINEL slaves <master name> ：列出给定主服务器的所有从服务器，以及这些从服务器的当前状态
     *
     * 127.0.0.1:26380> sentinel slaves myMaster
     * 1)  1) "name"
     *     2) "127.0.0.1:6382"
     *     3) "ip"
     *     4) "127.0.0.1"
     *     5) "port"
     *     6) "6382"
     *     7) "runid"
     *     8) "c6575c041f4397b19b394db2043555269496d539"
     *     9) "flags"
     *    10) "slave"
     *    11) "link-pending-commands"
     *    12) "0"
     *    13) "link-refcount"
     *    14) "1"
     *    15) "last-ping-sent"
     *    16) "0"
     *    17) "last-ok-ping-reply"
     *    18) "813"
     *    19) "last-ping-reply"
     *    20) "813"
     *    21) "down-after-milliseconds"
     *    22) "30000"
     *    23) "info-refresh"
     *    24) "1683"
     *    25) "role-reported"
     *    26) "slave"
     *    27) "role-reported-time"
     *    28) "55250628"
     *    29) "master-link-down-time"
     *    30) "0"
     *    31) "master-link-status"
     *    32) "ok"
     *    33) "master-host"
     *    34) "127.0.0.1"
     *    35) "master-port"
     *    36) "6381"
     *    37) "slave-priority"
     *    38) "100"
     *    39) "slave-repl-offset"
     *    40) "881052"
     * 2)  1) "name"
     *     2) "127.0.0.1:6380"
     *     3) "ip"
     *     4) "127.0.0.1"
     *     5) "port"
     *     6) "6380"
     *     7) "runid"
     *     8) ""
     *     9) "flags"
     *    10) "s_down,slave,disconnected"
     *    11) "link-pending-commands"
     *    12) "3"
     *    13) "link-refcount"
     *    14) "1"
     *    15) "last-ping-sent"
     *    16) "55250628"
     *    17) "last-ok-ping-reply"
     *    18) "55250628"
     *    19) "last-ping-reply"
     *    20) "55250628"
     *    21) "s-down-time"
     *    22) "55220593"
     *    23) "down-after-milliseconds"
     *    24) "30000"
     *    25) "info-refresh"
     *    26) "1600041915616"
     *    27) "role-reported"
     *    28) "slave"
     *    29) "role-reported-time"
     *    30) "55250628"
     *    31) "master-link-down-time"
     *    32) "0"
     *    33) "master-link-status"
     *    34) "err"
     *    35) "master-host"
     *    36) "?"
     *    37) "master-port"
     *    38) "0"
     *    39) "slave-priority"
     *    40) "100"
     *    41) "slave-repl-offset"
     *    42) "0"
     *
     * @param masterName
     * @return
     */
    List<Map<String, String>> sentinelSlaves(String masterName);

    /**
     * SENTINEL get-master-addr-by-name <master name>：返回给定名字的主服务器的ip地址和端口号，如果这个主服务器正在执行故障转移操作，
     * 或者针对这个主服务器的故障转移操作已经完成，那么这个命令返回新的主服务器的 IP 地址和端口号。
     *
     * 127.0.0.1:26380> sentinel get-master-addr-by-name myMaster
     *
     * 1) "127.0.0.1"
     * 2) "6381"
     *
     * 根据redis主服务器名称，获取主服务器的master地址和端口
     *
     * @param masterName
     * @return
     */
    List<String> sentinelGetMasterAddrByName(String masterName);

    /**
     * SENTINEL reset <pattern>：重置所有名字和给定模式pattern相匹配的主服务器，pattern参数是一个Glob风格的模式，
     * 重置操作清除主服务器目前的所有状态，包括正在执行中的故障转移，并移除目前已经发现和关联的主服务器的所有从服务器和Sentinel
     *
     *
     * @param pattern
     * @return
     */
    Long sentinelReset(String pattern);

    /**
     * SENTINEL failover <master name>：当主服务器失效时， 在不询问其他 Sentinel 意见的情况下， 强制开始一次自动故障迁移
     * （不过发起故障转移的 Sentinel 会向其他 Sentinel 发送一个新的配置，其他 Sentinel 会根据这个配置进行相应的更新）。
     *
     * 127.0.0.1:26380> SENTINEL failover myMaster
     * OK
     *
     * @param masterName
     * @return
     */
    String sentinelFailover(String masterName);

    /**
     * 127.0.0.1:26380> sentinel monitor ourMaster 127.0.0.1 6380 2
     * OK
     *
     * 给主机起的名字(不重即可)；
     * 2：表示当2个sentinel实例都认为master失效时，正式失效。
     *
     * @param masterName
     * @param ip
     * @param port
     * @param quorum
     * @return
     */
    String sentinelMonitor(String masterName, String ip, int port, int quorum);

    /**
     * 命令sentinel放弃对某个master的监听
     *
     * 127.0.0.1:26380> sentinel remove ourMaster
     * OK
     *
     * @param masterName
     * @return
     */
    String sentinelRemove(String masterName);

    /**
     *
     * @param masterName
     * @param parameterMap
     * @return
     */
    String sentinelSet(String masterName, Map<String, String> parameterMap);

}
