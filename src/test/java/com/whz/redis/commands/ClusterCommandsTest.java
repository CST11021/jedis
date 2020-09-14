package com.whz.redis.commands;

import com.whz.redis.RedisClientUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @Author: wanghz
 * @Date: 2020/9/14 8:26 AM
 */
public class ClusterCommandsTest {

    private Jedis jedis = RedisClientUtil.getJedis("127.0.0.1", 6380);

    @Test
    public void clusterNodes() {
        System.out.println(jedis.clusterNodes());
    }
}
