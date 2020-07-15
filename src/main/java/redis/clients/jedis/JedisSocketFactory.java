package redis.clients.jedis;

import java.io.IOException;
import java.net.Socket;

/**
 * JedisSocketFactory：负责从Jedis客户端中创建套接字连接，默认的套接字工厂将使用推荐的配置创建TCP套接字。
 * 您可以将自定义JedisSocketFactory用于许多用例，例如：
 * - a custom address resolver
 * - a unix domain socket
 * - a custom configuration for you TCP sockets
 */
public interface JedisSocketFactory {

    /**
     * 创建一个socket
     *
     * @return
     * @throws IOException
     */
    Socket createSocket() throws IOException;

    /**
     * 返回服务端host及端口信息
     *
     * @return
     */
    String getDescription();

    String getHost();
    void setHost(String host);

    int getPort();
    void setPort(int port);

    /**
     * 指的是连接一个url的连接等待时间
     *
     * @return
     */
    int getConnectionTimeout();
    void setConnectionTimeout(int connectionTimeout);

    /**
     * 读取数据超时时间
     *
     * @return
     */
    int getSoTimeout();
    void setSoTimeout(int soTimeout);
}
