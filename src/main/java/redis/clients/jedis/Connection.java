package redis.clients.jedis;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.util.IOUtils;
import redis.clients.jedis.util.RedisInputStream;
import redis.clients.jedis.util.RedisOutputStream;
import redis.clients.jedis.util.SafeEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示与redis服务器的连接对象
 */
public class Connection implements Closeable {

    private static final byte[][] EMPTY_ARGS = new byte[0][];

    /** 当socket创建异常时，该值置为false */
    private boolean broken = false;
    /** JedisSocketFactory创建的socket，表示一个Connection连接 */
    private Socket socket;
    /** 用于创建连接redis服务器的socket工厂 */
    private JedisSocketFactory jedisSocketFactory;
    /** 表示socket输出流，客户端通过该对象发送redis命令 */
    private RedisOutputStream outputStream;
    /** 表示socket输入流，客户端通过该对象获取redis返回的信息 */
    private RedisInputStream inputStream;


    public Connection() {
        this(Protocol.DEFAULT_HOST);
    }
    public Connection(final String host) {
        this(host, Protocol.DEFAULT_PORT);
    }
    public Connection(final String host, final int port) {
        this(host, port, false);
    }
    public Connection(final String host, final int port, final boolean ssl) {
        this(host, port, ssl, null, null, null);
    }
    public Connection(final String host, final int port, final boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        this(new DefaultJedisSocketFactory(host, port, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT, ssl, sslSocketFactory, sslParameters, hostnameVerifier));
    }
    public Connection(final JedisSocketFactory jedisSocketFactory) {
        this.jedisSocketFactory = jedisSocketFactory;
    }





    /**
     * 如果连接断开，则重连，并将soTimeout设置为0
     */
    public void setTimeoutInfinite() {
        try {
            if (!isConnected()) {
                connect();
            }
            socket.setSoTimeout(0);
        } catch (SocketException ex) {
            broken = true;
            throw new JedisConnectionException(ex);
        }
    }
    /**
     * 设置当前socket的soTimeout时间
     */
    public void rollbackTimeout() {
        try {
            socket.setSoTimeout(jedisSocketFactory.getSoTimeout());
        } catch (SocketException ex) {
            broken = true;
            throw new JedisConnectionException(ex);
        }
    }


    // 往redis服务端发送命令

    public void sendCommand(final ProtocolCommand cmd) {
        sendCommand(cmd, EMPTY_ARGS);
    }
    public void sendCommand(final ProtocolCommand cmd, final String... args) {
        final byte[][] bargs = new byte[args.length][];
        for (int i = 0; i < args.length; i++) {
            bargs[i] = SafeEncoder.encode(args[i]);
        }
        sendCommand(cmd, bargs);
    }
    /**
     *
     * @param cmd
     * @param args
     */
    public void sendCommand(final ProtocolCommand cmd, final byte[]... args) {
        try {
            // 创建一个socket连接
            connect();
            // 将命令发送到redis服务器
            Protocol.sendCommand(outputStream, cmd, args);
        } catch (JedisConnectionException ex) {
            // 当客户端发送由无效协议形成的请求时，Redis在关闭连接之前发送回错误消息。我们尝试阅读以提供失败原因.
            try {
                String errorMessage = Protocol.readErrorLineIfPossible(inputStream);
                if (errorMessage != null && errorMessage.length() > 0) {
                    ex = new JedisConnectionException(errorMessage, ex.getCause());
                }
            } catch (Exception e) {
                // 捕获InputStream＃read中发生的任何IOException或JedisConnectionException并忽略。这种方法是安全的，因为读取错误消息是可选的，并且连接最终将关闭。
            }
            // Any other exceptions related to connection?
            broken = true;
            throw ex;
        }
    }




    // 操作连接相关

    public boolean isBroken() {
        return broken;
    }
    public boolean isConnected() {
        return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected()
                && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }
    /**
     * 创建一个socket连接
     */
    public void connect() {
        if (!isConnected()) {
            try {
                socket = jedisSocketFactory.createSocket();

                outputStream = new RedisOutputStream(socket.getOutputStream());
                inputStream = new RedisInputStream(socket.getInputStream());
            } catch (IOException ex) {
                broken = true;
                throw new JedisConnectionException("Failed connecting to " + jedisSocketFactory.getDescription(), ex);
            }
        }
    }
    public void disconnect() {
        if (isConnected()) {
            try {
                outputStream.flush();
                socket.close();
            } catch (IOException ex) {
                broken = true;
                throw new JedisConnectionException(ex);
            } finally {
                IOUtils.closeQuietly(socket);
            }
        }
    }
    @Override
    public void close() {
        disconnect();
    }

    /**
     * 为了提高处理效率，write是写到缓冲区中，当缓冲区满或close时系统会自动将缓冲区的内容写入文件，所以一般是不需要调用flush的，不过如果你需要使write马上写入到文件中，就需要调用flush
     */
    protected void flush() {
        try {
            outputStream.flush();
        } catch (IOException ex) {
            broken = true;
            throw new JedisConnectionException(ex);
        }
    }





    // 从输入流程读取redis返回的信息

    /**
     * 读取reids返回的状态码
     *
     * @return
     */
    public String getStatusCodeReply() {
        flush();
        // 从输入流程读取redis返回的信息
        final byte[] resp = (byte[]) readProtocolWithCheckingBroken();
        if (null == resp) {
            return null;
        } else {
            return SafeEncoder.encode(resp);
        }
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    public String getBulkReply() {
        // 从输入流程读取redis返回的信息
        final byte[] result = getBinaryBulkReply();
        if (null != result) {
            return SafeEncoder.encode(result);
        } else {
            return null;
        }
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    public List<String> getMultiBulkReply() {
        return BuilderFactory.STRING_LIST.build(getBinaryMultiBulkReply());
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    public byte[] getBinaryBulkReply() {
        flush();
        return (byte[]) readProtocolWithCheckingBroken();
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<byte[]> getBinaryMultiBulkReply() {
        flush();
        return (List<byte[]>) readProtocolWithCheckingBroken();
    }
    @Deprecated
    public List<Object> getRawObjectMultiBulkReply() {
        return getUnflushedObjectMultiBulkReply();
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Object> getUnflushedObjectMultiBulkReply() {
        return (List<Object>) readProtocolWithCheckingBroken();
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    public List<Object> getObjectMultiBulkReply() {
        flush();
        return getUnflushedObjectMultiBulkReply();
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    public Long getIntegerReply() {
        flush();
        return (Long) readProtocolWithCheckingBroken();
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Long> getIntegerMultiBulkReply() {
        flush();
        return (List<Long>) readProtocolWithCheckingBroken();
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    public Object getOne() {
        flush();
        return readProtocolWithCheckingBroken();
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @param count
     * @return
     */
    public List<Object> getMany(final int count) {
        flush();
        final List<Object> responses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            try {
                responses.add(readProtocolWithCheckingBroken());
            } catch (JedisDataException e) {
                responses.add(e);
            }
        }
        return responses;
    }
    /**
     * 从输入流程读取redis返回的信息
     *
     * @return
     */
    protected Object readProtocolWithCheckingBroken() {
        if (broken) {
            throw new JedisConnectionException("Attempting to read from a broken connection");
        }

        try {
            return Protocol.read(inputStream);
        } catch (JedisConnectionException exc) {
            broken = true;
            throw exc;
        }
    }







    // jedisSocketFactory：host、port、connectionTimeout、soTimeout

    public String getHost() {
        return jedisSocketFactory.getHost();
    }
    public void setHost(final String host) {
        jedisSocketFactory.setHost(host);
    }
    public int getPort() {
        return jedisSocketFactory.getPort();
    }
    public void setPort(final int port) {
        jedisSocketFactory.setPort(port);
    }
    public int getConnectionTimeout() {
        return jedisSocketFactory.getConnectionTimeout();
    }
    public void setConnectionTimeout(int connectionTimeout) {
        jedisSocketFactory.setConnectionTimeout(connectionTimeout);
    }
    public int getSoTimeout() {
        return jedisSocketFactory.getSoTimeout();
    }
    public void setSoTimeout(int soTimeout) {
        jedisSocketFactory.setSoTimeout(soTimeout);
    }

    public Socket getSocket() {
        return socket;
    }
}
