package redis.clients.jedis;

import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DefaultJedisSocketFactory implements JedisSocketFactory {

    /** redis host */
    private String host;
    /** reids port */
    private int port;
    /** 连接redis的等待超时时间 */
    private int connectionTimeout;
    /** 读取redis response 数据超时时间 */
    private int soTimeout;

    /** 是否ssl */
    private boolean ssl;
    /** 用于创建SSLSocket的工厂 */
    private SSLSocketFactory sslSocketFactory;
    /** 关于SSLSocket相关的参数设置 */
    private SSLParameters sslParameters;
    /** 关于HostnameVerifier接口的作用（一般在SSL模式下才有用到）：https://www.cnblogs.com/yufecheng/p/10968045.html */
    private HostnameVerifier hostnameVerifier;

    /**
     *
     *
     * @param host                  redis服务端host
     * @param port                  redis服务端端口
     * @param connectionTimeout     连接超时时间
     * @param soTimeout
     * @param ssl
     * @param sslSocketFactory
     * @param sslParameters
     * @param hostnameVerifier
     */
    public DefaultJedisSocketFactory(String host, int port, int connectionTimeout, int soTimeout,
                                     boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                                     HostnameVerifier hostnameVerifier) {
        this.host = host;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket();
            // 启动端口重用，这行代码一定要放到绑定端口前：
            // 该选项用来决定如果网络上仍然有数据向旧的ServerSocket传输数据，是否允许新的ServerSocket绑定到与旧的ServerSocket同样的
            // 端口上，该选项的默认值与操作系统有关，在某些操作系统中，允许重用端口，而在某些系统中不允许重用端口。当ServerSocket关闭时，
            // 如果网络上还有发送到这个serversocket上的数据，这个ServerSocket不会立即释放本地端口，而是等待一段时间，确保接收到了网络
            // 上发送过来的延迟数据，然后再释放端口。值得注意的是，public void setReuseAddress(boolean on) throws SocketException
            // 必须在ServerSocket还没有绑定到一个本地端口之前使用，否则执行该方法无效。此外，两个公用同一个端口的进程必须都调用
            // serverSocket.setReuseAddress(true)方法，才能使得一个进程关闭ServerSocket之后，另一个进程的ServerSocket还能够立刻重用相同的端口。
            socket.setReuseAddress(true);
            // 其实这个选项的意思是TCP连接空闲时是否需要向对方发送探测包，实际上是依赖于底层的TCP模块实现的，java中只能设置是否开启，
            // 不能设置其详细参数，只能依赖于系统配置。keepalive 不是说TCP的长连接，当我们作为服务端，一个客户端连接上来，如果设置了
            // keeplive为 true，当对方没有发送任何数据过来，超过一个时间(看系统内核参数配置)，那么我们这边会发送一个ack探测包发到对方，
            // 探测双方的TCP/IP连接是否有效(对方可能断点，断网)。如果不设置，那么客户端宕机时，服务器永远也不知道客户端宕机了，仍然保存这个失效的连接。
            // 当然，在客户端也可以使用这个参数。客户端Socket会每隔段的时间（大约两个小时）就会利用空闲的连接向服务器发送一个数据包。这个
            // 数据包并没有其它的作用，只是为了检测一下服务器是否仍处于活动状态。如果服务器未响应这个数据包，在大约11分钟后，客户端Socket
            // 再发送一个数据包，如果在12分钟内，服务器还没响应，那么客户端Socket将关闭。如果将Socket选项关闭，客户端Socket在服务器无效
            // 的情况下可能会长时间不会关闭。
            socket.setKeepAlive(true);
            // 禁用纳格算法，将数据立即发送出去。纳格算法是以减少封包传送量来增进TCP/IP网络的效能，当我们调用下面代码，如:
            //
            // Socket socket = new Socket();
            // socket.connect(new InetSocketAddress(host, 8000));
            // InputStream in = socket.getInputStream();
            // OutputStream out = socket.getOutputStream();
            // String head = "hello ";
            // String body = "world\r\n";
            // out.write(head.getBytes());
            // out.write(body.getBytes());
            // 我们发送了hello，当hello没有收到ack确认(TCP是可靠连接，发送的每一个数据都要收到对方的一个ack确认，否则就要重发)的时候，
            // 根据纳格算法，world不会立马发送，会等待，要么等到ack确认(最多等100ms对方会发过来的)，要么等到TCP缓冲区内容>=MSS，很明显
            // 这里没有机会，我们写了world后再也没有写数据了，所以只能等到hello的ack我们才会发送world，除非我们禁用纳格算法，数据就会立即发送了。
            socket.setTcpNoDelay(true);
            // 在Java Socket中，当我们调用Socket的close方法时，默认的行为是当底层网卡所有数据都发送完毕后，关闭连接，通过setSoLinger方法，我们可以修改close方法的行为

            // 1，setSoLinger(true, 0)
            // 当网卡收到关闭连接请求后，无论数据是否发送完毕，立即发送RST包关闭连接
            //
            // 2，setSoLinger(true, delay_time)
            // 当网卡收到关闭连接请求后，等待delay_time
            //
            // 如果在delay_time过程中数据发送完毕，正常四次挥手关闭连接；
            // 如果在delay_time过程中数据没有发送完毕，发送RST包关闭连接；
            socket.setSoLinger(true, 0);
            // 设置：redis的host、port和连接redis的等待超时时间
            socket.connect(new InetSocketAddress(getHost(), getPort()), getConnectionTimeout());
            // 读取redis response 数据超时时间
            socket.setSoTimeout(getSoTimeout());

            if (ssl) {
                if (null == sslSocketFactory) {
                    sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                }

                socket = sslSocketFactory.createSocket(socket, getHost(), getPort(), true);
                if (null != sslParameters) {
                    ((SSLSocket) socket).setSSLParameters(sslParameters);
                }

                // 检查公钥是否被调包
                if ((null != hostnameVerifier)
                        && (!hostnameVerifier.verify(getHost(), ((SSLSocket) socket).getSession()))) {
                    String message = String.format("The connection to '%s' failed ssl/tls hostname verification.", getHost());
                    throw new JedisConnectionException(message);
                }
            }
            return socket;
        } catch (Exception ex) {
            if (socket != null) {
                socket.close();
            }
            throw ex;
        }
    }

    @Override
    public String getDescription() {
        return host + ":" + port;
    }

    @Override
    public String getHost() {
        return host;
    }
    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }
    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    @Override
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public int getSoTimeout() {
        return soTimeout;
    }
    @Override
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }
}
