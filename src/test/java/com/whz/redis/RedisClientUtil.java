package com.whz.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisClientUtil {

    private static JedisPool jedisPool = null;

    private static String host = "127.0.0.1";
    private static Integer port = 6379;
    private static Integer timeout = 5 * 1000;
    private static String password = null;

    private RedisClientUtil() {

    }

    public static synchronized Jedis getJedis() {
        return getJedis(host, port);
    }

    public static synchronized Jedis getJedis(String host, Integer port) {
        if (jedisPool == null) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            //指定连接池中最大空闲连接数
            jedisPoolConfig.setMaxIdle(10);
            //链接池中创建的最大连接数
            jedisPoolConfig.setMaxTotal(100);
            //创建连接前先测试连接是否可用
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
        }

        return jedisPool.getResource();
    }
}