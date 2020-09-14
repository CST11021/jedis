package com.whz.redis.commands;

import com.whz.redis.RedisClientUtil;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * @Author: wanghz
 * @Date: 2020/9/14 7:42 AM
 */
public class SentinelCommandsTest {

    private Jedis jedis = RedisClientUtil.getJedis("127.0.0.1", 26380);

    @Test
    public void sentinelMasters() {
        List<Map<String, String>> info = jedis.sentinelMasters();
        for (Map<String, String> map : info) {
            for (Map.Entry<String, String> entry : map.entrySet())
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        // role-reported = master
        // info-refresh = 9624
        // config-epoch = 1
        // last-ping-sent = 0
        // role-reported-time = 54525769
        // ip = 127.0.0.1
        // quorum = 1
        // flags = master
        // parallel-syncs = 1
        // num-slaves = 2
        // link-pending-commands = 0
        // failover-timeout = 180000
        // port = 6381
        // num-other-sentinels = 0
        // name = myMaster
        // last-ok-ping-reply = 710
        // last-ping-reply = 710
        // runid = 68a74d3a6125186dbd9d3ad9c3f62414a7bb0bd7
        // link-refcount = 1
        // down-after-milliseconds = 30000
    }

    @Test
    public void sentinelGetMasterAddrByName() {
        List<String> list = jedis.sentinelGetMasterAddrByName("myMaster");
        for (String str : list) {
            System.out.println(str);
        }

        // 127.0.0.1
        // 6381
    }

}
