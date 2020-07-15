package redis.clients.jedis.util;

import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.UnsupportedEncodingException;

/**
 * 拥有此功能的唯一原因是能够与Java 1.5兼容 :(
 */
public final class SafeEncoder {

    private SafeEncoder() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * 将字符串转成字节
     *
     * @param strs
     * @return
     */
    public static byte[][] encodeMany(final String... strs) {
        byte[][] many = new byte[strs.length][];
        for (int i = 0; i < strs.length; i++) {
            many[i] = encode(strs[i]);
        }
        return many;
    }

    /**
     * 将字符串转成字节
     *
     * @param str
     * @return
     */
    public static byte[] encode(final String str) {
        try {
            if (str == null) {
                throw new JedisDataException("value sent to redis cannot be null");
            }
            return str.getBytes(Protocol.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new JedisException(e);
        }
    }

    /**
     * 将字节转为字符串
     *
     * @param data
     * @return
     */
    public static String encode(final byte[] data) {
        try {
            return new String(data, Protocol.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new JedisException(e);
        }
    }

}
