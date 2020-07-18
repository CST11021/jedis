package pubsub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JedisSubscribe extends JedisPubSub {

    // 接收消息

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("订阅消息回调1：channel：" + channel + ", message：" + message);
    }
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println("订阅消息回调2：pattern：" + pattern + ", channel：" + channel + "message：" + message);
    }

    // 订阅channel

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println("订阅频道1：channel: " + channel + ", subscribedChannels：" + subscribedChannels);
    }
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("订阅频道2：pattern: " + pattern + ", subscribedChannels：" + subscribedChannels);
    }

    // 取消订阅

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        System.out.println("取消订阅1: channel: " + channel + ", subscribedChannels：" + subscribedChannels);
    }
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println("取消订阅2：pattern: " + pattern + ", subscribedChannels：" + subscribedChannels);
    }

    public static void main(String[] args) throws IOException {

        Jedis jedis = RedisUtils.getJedis();
        JedisSubscribe jedisPubSub = new JedisSubscribe();
        // jedis.subscribe(jedisPubSub, "myChannel");
        jedis.psubscribe(jedisPubSub, "my*");


    }

}