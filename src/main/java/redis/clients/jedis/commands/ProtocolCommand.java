package redis.clients.jedis.commands;

/**
 * Command枚举类实现了该接口，通过getRaw()方法，返回命令对应的字节
 */
public interface ProtocolCommand {

    /**
     * 将命令转为字节返回
     *
     * @return
     */
    byte[] getRaw();

}
