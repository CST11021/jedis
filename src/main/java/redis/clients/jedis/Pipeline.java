package redis.clients.jedis;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.exceptions.JedisDataException;

/**
 * redis是一个cs模式的tcp server，使用和http类似的请求响应协议。一个client可以通过一个socket连接发起多个请求命令。每个请求命令发出后
 * client通常会阻塞并等待redis服务处理，redis处理完后请求命令后会将结果通过响应报文返回给client。所以在多条命令需要处理时，使用pipeline
 * 效率会快得多。通过pipeline方式当有大批量的操作时候。我们可以节省很多原来浪费在网络延迟的时间。pipeline方式将client端命令一起发出，
 * redis server会处理完多条命令后，将结果一起打包返回client,从而节省大量的网络延迟开销。需要注意到是用 pipeline方式打包命令发送，
 * redis必须在处理完所有命令前先缓存起所有命令的处理结果。打包的命令越多，缓存消耗内存也越多。所以并是不是打包的命令越多越好。具体多少合适需要根据具体情况测试。
 */
public class Pipeline extends MultiKeyPipelineBase implements Closeable {

  private MultiResponseBuilder currentMulti;

  private class MultiResponseBuilder extends Builder<List<Object>> {
    private List<Response<?>> responses = new ArrayList<>();

    @Override
    public List<Object> build(Object data) {
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>) data;
      List<Object> values = new ArrayList<>();

      if (list.size() != responses.size()) {
        throw new JedisDataException("Expected data size " + responses.size() + " but was "
            + list.size());
      }

      for (int i = 0; i < list.size(); i++) {
        Response<?> response = responses.get(i);
        response.set(list.get(i));
        Object builtResponse;
        try {
          builtResponse = response.get();
        } catch (JedisDataException e) {
          builtResponse = e;
        }
        values.add(builtResponse);
      }
      return values;
    }

    public void setResponseDependency(Response<?> dependency) {
      for (Response<?> response : responses) {
        response.setDependency(dependency);
      }
    }

    public void addResponse(Response<?> response) {
      responses.add(response);
    }
  }

  @Override
  protected <T> Response<T> getResponse(Builder<T> builder) {
    if (currentMulti != null) {
      super.getResponse(BuilderFactory.STRING); // Expected QUEUED

      Response<T> lr = new Response<>(builder);
      currentMulti.addResponse(lr);
      return lr;
    } else {
      return super.getResponse(builder);
    }
  }

  public void setClient(Client client) {
    this.client = client;
  }

  @Override
  protected Client getClient(byte[] key) {
    return client;
  }

  @Override
  protected Client getClient(String key) {
    return client;
  }

  public void clear() {
    if (isInMulti()) {
      discard();
    }

    sync();
  }

  public boolean isInMulti() {
    return currentMulti != null;
  }

  /**
   * Synchronize pipeline by reading all responses. This operation close the pipeline. In order to
   * get return values from pipelined commands, capture the different Response&lt;?&gt; of the
   * commands you execute.
   */
  public void sync() {
    if (getPipelinedResponseLength() > 0) {
      List<Object> unformatted = client.getMany(getPipelinedResponseLength());
      for (Object o : unformatted) {
        generateResponse(o);
      }
    }
  }

  /**
   * Synchronize pipeline by reading all responses. This operation close the pipeline. Whenever
   * possible try to avoid using this version and use Pipeline.sync() as it won't go through all the
   * responses and generate the right response type (usually it is a waste of time).
   * @return A list of all the responses in the order you executed them.
   */
  public List<Object> syncAndReturnAll() {
    if (getPipelinedResponseLength() > 0) {
      List<Object> unformatted = client.getMany(getPipelinedResponseLength());
      List<Object> formatted = new ArrayList<>();
      for (Object o : unformatted) {
        try {
          formatted.add(generateResponse(o).get());
        } catch (JedisDataException e) {
          formatted.add(e);
        }
      }
      return formatted;
    } else {
      return java.util.Collections.<Object> emptyList();
    }
  }

  public Response<String> discard() {
    if (currentMulti == null) throw new JedisDataException("DISCARD without MULTI");
    client.discard();
    currentMulti = null;
    return getResponse(BuilderFactory.STRING);
  }

  public Response<List<Object>> exec() {
    if (currentMulti == null) throw new JedisDataException("EXEC without MULTI");

    client.exec();
    Response<List<Object>> response = super.getResponse(currentMulti);
    currentMulti.setResponseDependency(response);
    currentMulti = null;
    return response;
  }

  public Response<String> multi() {
    if (currentMulti != null) throw new JedisDataException("MULTI calls can not be nested");

    client.multi();
    Response<String> response = getResponse(BuilderFactory.STRING); // Expecting
    // OK
    currentMulti = new MultiResponseBuilder();
    return response;
  }

  @Override
  public void close() {
    clear();
  }

}
