package pubsub;

import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JedisPublisher {

    public static void main(String[] args) throws IOException {

        // 获取一个Jedis客户端实例
        Jedis jedis = RedisUtils.getJedis();

        while (true) {
            // 发布消息
            String message = new BufferedReader(new InputStreamReader(System.in)).readLine();
            Long count = jedis.publish("myChannel", message);
            System.out.println("发送成功：" + count);
        }

    }

}