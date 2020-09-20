package redis.clients.jedis.commands;

import java.util.List;

public interface ScriptingCommands {

    /**
     * 关于reids 脚本命令，参考：http://doc.redisfans.com/script/eval.html#script
     *
     * @param script
     * @return
     */
    Object eval(String script);
    Object eval(String script, int keyCount, String... params);
    Object eval(String script, List<String> keys, List<String> args);
    Object evalsha(String sha1);
    Object evalsha(String sha1, List<String> keys, List<String> args);
    Object evalsha(String sha1, int keyCount, String... params);

    /**
     * 根据给定的脚本校验和，检查指定的脚本是否存在于脚本缓存
     *
     * @param sha1
     * @return
     */
    Boolean scriptExists(String sha1);

    /**
     * 根据给定的脚本校验和，检查指定的脚本是否存在于脚本缓存
     *
     * @param sha1
     * @return
     */
    List<Boolean> scriptExists(String... sha1);

    /**
     * Redis Script Load 命令用于将脚本添加到脚本缓存中，但并不立即执行这个脚本，EVAL命令也会将脚本添加到脚本缓存中，但是它会立即对输入的脚本进行求值。
     * 如果给定的脚本已经在缓存里面了，那么不执行任何操作，在脚本被加入到缓存之后，通过EVALSHA命令，可以使用脚本的SHA1校验和来调用这个脚本，
     * 脚本可以在缓存中保留无限长的时间，直到执行 SCRIPT FLUSH 为止
     *
     * redis 127.0.0.1:6379> SCRIPT LOAD "return 1"
     * "e0e1f9fabfc9d4800c877a703b823ac0578ff8db"
     *
     * @param script
     * @return
     */
    String scriptLoad(String script);

}
