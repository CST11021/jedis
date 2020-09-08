package com.whz.redis.expiredlistener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 测试Redis建过期事件监听：
 *
 * 1、Redis本身需要开启事件监听，配置事件监听规则，打开Redis配置文件，设置为：notify-keyspace-events Ex，默认是：notify-keyspace-events ""，不开启事件监听
 * 2、启动{@link Subscriber}订阅键过期事件监听
 * 3、启动{@link TestJedis} 设置一个键，并设置过期时间
 */
public class TestJedis {

    public static void main(String[] args) {

        JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

        Jedis jedis = pool.getResource();

        jedis.set("notify", "新浪微博：小叶子一点也不逗");

        jedis.expire("notify", 10);

    }

}