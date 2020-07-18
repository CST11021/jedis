package hyperloglog;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @Author: wanghz
 * @Date: 2020/7/15 7:35 PM
 */
public class HyperLogLogTest {

    private Jedis jedis;

    @Before
    public void setup() {
        // 连接redis服务器，192.168.0.100:6379
        jedis = new Jedis("localhost", 6379);
    }

    @Test
    public void test() throws InterruptedException {
        jedis.pfadd("test", "a");
    }

}
