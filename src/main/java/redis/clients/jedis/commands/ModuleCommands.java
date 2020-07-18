package redis.clients.jedis.commands;

import redis.clients.jedis.Module;

import java.util.List;

/**
 * ModuleCommands：提供redis模块加载和卸载命令
 *
 * 通过模块可以自定义扩展redis命令
 */
public interface ModuleCommands {

    String moduleLoad(String path);

    String moduleUnload(String name);

    List<Module> moduleList();

}
