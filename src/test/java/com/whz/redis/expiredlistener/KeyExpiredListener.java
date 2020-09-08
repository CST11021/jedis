package com.whz.redis.expiredlistener;

import redis.clients.jedis.JedisPubSub;

public class KeyExpiredListener extends JedisPubSub {

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("订阅事件，pattern：" + pattern + ", channels：" + subscribedChannels);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        // 只能获取到失效的key，但是此时不能根据key获取value值，因为该事件是在数据失效后才触发
        System.out.println("事件回调，pattern：" + pattern + "，channel：" + channel + "， message：" + message);
    }

}