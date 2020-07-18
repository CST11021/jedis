package redis.clients.jedis;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.SafeEncoder;

import java.util.Arrays;
import java.util.List;

import static redis.clients.jedis.Protocol.Keyword.*;

/**
 * 对Jedis订阅者的封装，Jedis订阅者可以通过继承该类，实现订阅
 */
public abstract class JedisPubSub {

    private static final String JEDIS_SUBSCRIPTION_MESSAGE = "JedisPubSub is not subscribed to a Jedis instance.";
    /** reids客户端 */
    private volatile Client client;
    /** 表示当前客户端实例订阅的channel数量 */
    private int subscribedChannels = 0;


    /****************/
    /** 6个回调接口 **/
    /****************/

    // 1、非pattern匹配的订阅
    /**
     * 订阅消息的回调接口，当发布者发布消息后，会调用该接口
     *
     * @param channel
     * @param message
     */
    public void onMessage(String channel, String message) {
    }
    /**
     * 订阅channel时的回调接口，订相应的channel被订阅时，会调用该接口
     *
     * @param channel               订阅的channel
     * @param subscribedChannels    订阅该channel的订阅者数量
     */
    public void onSubscribe(String channel, int subscribedChannels) {
    }
    public void onUnsubscribe(String channel, int subscribedChannels) {

    }


    // 2、pattern匹配的订阅
    /**
     * 订阅消息的回调接口，当发布者发布消息后，会调用该接口
     *
     * @param pattern
     * @param channel
     * @param message
     */
    public void onPMessage(String pattern, String channel, String message) {
    }
    public void onPUnsubscribe(String pattern, int subscribedChannels) {

    }
    public void onPSubscribe(String pattern, int subscribedChannels) {

    }


    /**
     * 发送一个ping命令
     */
    public void ping() {
        if (client == null) {
            throw new JedisConnectionException(JEDIS_SUBSCRIPTION_MESSAGE);
        }
        client.ping();
        client.flush();
    }
    /**
     * ping命令回调
     *
     * @param pattern
     */
    public void onPong(String pattern) {

    }


    // 订阅channel

    public void subscribe(String... channels) {
        if (client == null) {
            throw new JedisConnectionException(JEDIS_SUBSCRIPTION_MESSAGE);
        }
        client.subscribe(channels);
        client.flush();
    }
    public void psubscribe(String... patterns) {
        if (client == null) {
            throw new JedisConnectionException(JEDIS_SUBSCRIPTION_MESSAGE);
        }
        client.psubscribe(patterns);
        client.flush();
    }

    // 取消订阅

    public void unsubscribe() {
        if (client == null) {
            throw new JedisConnectionException(JEDIS_SUBSCRIPTION_MESSAGE);
        }
        client.unsubscribe();
        client.flush();
    }
    public void unsubscribe(String... channels) {
        if (client == null) {
            throw new JedisConnectionException(JEDIS_SUBSCRIPTION_MESSAGE);
        }
        client.unsubscribe(channels);
        client.flush();
    }
    public void punsubscribe() {
        if (client == null) {
            throw new JedisConnectionException(JEDIS_SUBSCRIPTION_MESSAGE);
        }
        client.punsubscribe();
        client.flush();
    }
    public void punsubscribe(String... patterns) {
        if (client == null) {
            throw new JedisConnectionException(JEDIS_SUBSCRIPTION_MESSAGE);
        }
        client.punsubscribe(patterns);
        client.flush();
    }


    /**
     * 当Jedis订阅channel的时候（参考：{@link Jedis#subscribe(JedisPubSub, String...)}），会调用该方法，处理订阅命令
     *
     * @param client
     * @param channels
     */
    public void proceed(Client client, String... channels) {
        this.client = client;
        client.subscribe(channels);
        client.flush();
        process(client);
    }
    /**
     * 处理模式订阅
     *
     * @param client
     * @param patterns
     */
    public void proceedWithPatterns(Client client, String... patterns) {
        this.client = client;
        client.psubscribe(patterns);
        client.flush();
        process(client);
    }
    /**
     * 处理Redis订阅相关命令的返回信息，并调用相应的回调方法
     *
     * @param client
     */
    private void process(Client client) {

        do {
            // 读取redis服务返回的信息
            List<Object> reply = client.getUnflushedObjectMultiBulkReply();
            final Object firstObj = reply.get(0);
            if (!(firstObj instanceof byte[])) {
                throw new JedisException("Unknown message type: " + firstObj);
            }

            final byte[] resp = (byte[]) firstObj;
            if (Arrays.equals(SUBSCRIBE.raw, resp)) {
                subscribedChannels = ((Long) reply.get(2)).intValue();
                final byte[] bchannel = (byte[]) reply.get(1);
                final String strchannel = (bchannel == null) ? null : SafeEncoder.encode(bchannel);
                onSubscribe(strchannel, subscribedChannels);
            } else if (Arrays.equals(UNSUBSCRIBE.raw, resp)) {
                subscribedChannels = ((Long) reply.get(2)).intValue();
                final byte[] bchannel = (byte[]) reply.get(1);
                final String strchannel = (bchannel == null) ? null : SafeEncoder.encode(bchannel);
                onUnsubscribe(strchannel, subscribedChannels);
            } else if (Arrays.equals(MESSAGE.raw, resp)) {
                final byte[] bchannel = (byte[]) reply.get(1);
                final byte[] bmesg = (byte[]) reply.get(2);
                final String strchannel = (bchannel == null) ? null : SafeEncoder.encode(bchannel);
                final String strmesg = (bmesg == null) ? null : SafeEncoder.encode(bmesg);
                onMessage(strchannel, strmesg);
            } else if (Arrays.equals(PMESSAGE.raw, resp)) {
                final byte[] bpattern = (byte[]) reply.get(1);
                final byte[] bchannel = (byte[]) reply.get(2);
                final byte[] bmesg = (byte[]) reply.get(3);
                final String strpattern = (bpattern == null) ? null : SafeEncoder.encode(bpattern);
                final String strchannel = (bchannel == null) ? null : SafeEncoder.encode(bchannel);
                final String strmesg = (bmesg == null) ? null : SafeEncoder.encode(bmesg);
                onPMessage(strpattern, strchannel, strmesg);
            } else if (Arrays.equals(PSUBSCRIBE.raw, resp)) {
                subscribedChannels = ((Long) reply.get(2)).intValue();
                final byte[] bpattern = (byte[]) reply.get(1);
                final String strpattern = (bpattern == null) ? null : SafeEncoder.encode(bpattern);
                onPSubscribe(strpattern, subscribedChannels);
            } else if (Arrays.equals(PUNSUBSCRIBE.raw, resp)) {
                subscribedChannels = ((Long) reply.get(2)).intValue();
                final byte[] bpattern = (byte[]) reply.get(1);
                final String strpattern = (bpattern == null) ? null : SafeEncoder.encode(bpattern);
                onPUnsubscribe(strpattern, subscribedChannels);
            } else if (Arrays.equals(PONG.raw, resp)) {
                final byte[] bpattern = (byte[]) reply.get(1);
                final String strpattern = (bpattern == null) ? null : SafeEncoder.encode(bpattern);
                onPong(strpattern);
            } else {
                throw new JedisException("Unknown message type: " + firstObj);
            }
        } while (isSubscribed());

        /* Invalidate instance since this thread is no longer listening */
        this.client = null;
    }

    /**
     * 当前客户端是否有订阅channel
     *
     * @return
     */
    public boolean isSubscribed() {
        return subscribedChannels > 0;
    }
    /**
     * 返回当前客户端实例订阅的channel数量
     *
     * @return
     */
    public int getSubscribedChannels() {
        return subscribedChannels;
    }
}
