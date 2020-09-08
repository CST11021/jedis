package com.whz.redis.commands;

import com.whz.redis.RedisClientUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestRedis {

    private Jedis jedis = RedisClientUtil.getJedis();

    // @Before
    // public void setup() {
    //     // 连接redis服务器，192.168.0.100:6379
    //     jedis = new Jedis("localhost", 6379);
    //     // 权限认证
    //     // jedis.auth("admin");
    // }

    /**
     * ping
     */
    @Test
    public void testPing() {

        // ping
        String result = jedis.ping();
        Assert.assertEquals(result, "PONG");

        // ping test123
        String result1 = jedis.ping("test123");
        Assert.assertEquals(result1, "test123");

    }

    @Test
    public void testGetDb() {
        Assert.assertEquals(jedis.getDB(), 0);
    }

    @Test
    public void testInfo() {
        String info = jedis.info();
        String info0 = jedis.info("all");
        String info1 = jedis.info("server");
        String info2 = jedis.info("cpu");

        System.out.println();
    }

    @Test
    public void testSetbit() {

        jedis.setbit("setbitkey1", 0, true);
        jedis.setbit("setbitkey1", 1, false);
        Assert.assertEquals( true, jedis.getbit("setbitkey1", 0));
        Assert.assertEquals( false, jedis.getbit("setbitkey1", 1));

        jedis.setbit("setbitkey2", 0, "1");
        jedis.setbit("setbitkey2", 1, "0");
        Assert.assertEquals( true, jedis.getbit("setbitkey2", 0));
        Assert.assertEquals( false, jedis.getbit("setbitkey2", 1));

        Assert.assertEquals("string", jedis.type("setbitkey1"));
        Assert.assertEquals("string", jedis.type("setbitkey2"));

    }

    @Test
    public void testBitcount() {
        jedis.set("key_a", "a");// 01100001
        long count = jedis.bitcount("key_a");
        Assert.assertEquals(3L, count);
    }

    /**
     * redis存储字符串
     */
    @Test
    public void testString() {
        // set值：set name "xinxin"
        jedis.set("name", "xinxin");

        // 字符串拼接：append name " is my lover"
        jedis.append("name", " is my lover");

        // get name
        Assert.assertEquals(jedis.get("name"), "xinxin is my lover");

        // mget name test
        List<String> result = jedis.mget(new String[] {"name", "test"});
        Assert.assertEquals(result.get(0), "xinxin is my lover");
        Assert.assertEquals(result.get(1), null);

        // del name
        jedis.del("name");

    }

    /**
     * redis操作Map
     */
    @Test
    public void testMap() {
        //-----添加数据----------
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "xinxin");
        map.put("age", "22");
        map.put("qq", "123456");
        jedis.hmset("user", map);
        //取出user中的name，执行结果:[minxr]-->注意结果是一个泛型的List
        //第一个参数是存入redis中map对象的key，后面跟的是放入map中的对象的key，后面的key可以跟多个，是可变参数
        List<String> rsmap = jedis.hmget("user", "name", "qq");
        System.out.println(rsmap);

        //删除map中的某个键值
        jedis.hdel("user", "age");
        System.out.println(jedis.hmget("user", "age")); //因为删除了，所以返回的是null
        System.out.println(jedis.hlen("user")); //返回key为user的键中存放的值的个数2
        System.out.println(jedis.exists("user"));//是否存在key为user的记录 返回true
        System.out.println(jedis.hkeys("user"));//返回map对象中的所有key
        System.out.println(jedis.hvals("user"));//返回map对象中的所有value

        Iterator<String> iter = jedis.hkeys("user").iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            System.out.println(key + ":" + jedis.hmget("user", key));
        }
    }

    /**
     * jedis操作List
     */
    @Test
    public void testList() {
        //开始前，先移除所有的内容
        jedis.del("java framework");
        // System.out.println(jedis.lrange("java framework", -1));
        //先向key java framework中存放三条数据
        jedis.lpush("java framework", "spring");
        jedis.lpush("java framework", "struts");
        jedis.lpush("java framework", "hibernate");
        //再取出所有数据jedis.lrange是按范围取出，
        // 第一个是key，第二个是起始位置，第三个是结束位置，jedis.llen获取长度 -1表示取得所有
        // System.out.println(jedis.lrange("java framework", -1));

        jedis.del("java framework");
        jedis.rpush("java framework", "spring");
        jedis.rpush("java framework", "struts");
        jedis.rpush("java framework", "hibernate");
        // System.out.println(jedis.lrange("java framework", -1));
    }

    /**
     * jedis操作Set
     */
    @Test
    public void testSet() {
        //添加
        jedis.sadd("user", "liuling");
        jedis.sadd("user", "xinxin");
        jedis.sadd("user", "ling");
        jedis.sadd("user", "zhangxinxin");
        jedis.sadd("user", "who");
        //移除noname
        jedis.srem("user", "who");
        System.out.println(jedis.smembers("user"));//获取所有加入的value
        System.out.println(jedis.sismember("user", "who"));//判断 who 是否是user集合的元素
        System.out.println(jedis.srandmember("user"));
        System.out.println(jedis.scard("user"));//返回集合的元素个数
    }

    @Test
    public void test() throws InterruptedException {
        //jedis 排序
        //注意，此处的rpush和lpush是List的操作。是一个双向链表（但从表现来看的）
        jedis.del("a");//先清除数据，再加入数据进行测试
        jedis.rpush("a", "1");
        jedis.lpush("a", "6");
        jedis.lpush("a", "3");
        jedis.lpush("a", "9");
        // System.out.println(jedis.lrange("a", -1));// [9,3,6,1]
        System.out.println(jedis.sort("a")); //[1,9] //输入排序后结果
        // System.out.println(jedis.lrange("a", -1));
    }

}