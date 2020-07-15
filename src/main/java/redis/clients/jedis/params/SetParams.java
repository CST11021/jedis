package redis.clients.jedis.params;

import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;

public class SetParams extends Params {

    private static final String XX = "xx";
    private static final String NX = "nx";
    private static final String PX = "px";
    private static final String EX = "ex";

    public SetParams() {
    }

    public static SetParams setParams() {
        return new SetParams();
    }

    /**
     * 设置指定的过期时间（以秒为单位）。
     *
     * @param secondsToExpire
     * @return SetParams
     */
    public SetParams ex(int secondsToExpire) {
        addParam(EX, secondsToExpire);
        return this;
    }

    /**
     * 设置指定的过期时间（以毫秒为单位）。
     *
     * @param millisecondsToExpire
     * @return SetParams
     */
    public SetParams px(long millisecondsToExpire) {
        addParam(PX, millisecondsToExpire);
        return this;
    }

    /**
     * 仅在不存在的情况下设置密钥
     *
     * @return SetParams
     */
    public SetParams nx() {
        addParam(NX);
        return this;
    }

    /**
     * 仅设置密钥（如果已存在）.
     *
     * @return SetParams
     */
    public SetParams xx() {
        addParam(XX);
        return this;
    }

    public byte[][] getByteParams(byte[]... args) {
        ArrayList<byte[]> byteParams = new ArrayList<>();
        for (byte[] arg : args) {
            byteParams.add(arg);
        }

        if (contains(NX)) {
            byteParams.add(SafeEncoder.encode(NX));
        }
        if (contains(XX)) {
            byteParams.add(SafeEncoder.encode(XX));
        }

        if (contains(EX)) {
            byteParams.add(SafeEncoder.encode(EX));
            byteParams.add(Protocol.toByteArray((int) getParam(EX)));
        }
        if (contains(PX)) {
            byteParams.add(SafeEncoder.encode(PX));
            byteParams.add(Protocol.toByteArray((long) getParam(PX)));
        }

        return byteParams.toArray(new byte[byteParams.size()][]);
    }

}
