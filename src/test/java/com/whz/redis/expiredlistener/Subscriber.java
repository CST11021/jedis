package com.whz.redis.expiredlistener;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;

/**
 * 订阅键过期事件监听
 */
public class Subscriber {

    public static void main(String[] args) throws IOException {

        JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

        Jedis jedis = pool.getResource();

        jedis.psubscribe(new KeyExpiredListener(), "__key*__:*");

        System.in.read();

    }

}