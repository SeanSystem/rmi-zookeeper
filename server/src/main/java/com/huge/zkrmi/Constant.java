package com.huge.zkrmi;

/**
 * 常量
 */
public interface Constant {

    /**
     * ZooKeeper连接地址
     */
    String ZK_CONNECT_STRING  = "114.115.217.85:2182";
    /**
     * 连接会话过期时间
     */
    int ZK_SESSION_TIMEOUT = 5000;
    /**
     * 注册地址
     */
    String ZK_REGISTRY_PATH = "/registry";
    /**
     * 服务存放地址
     */
    String ZK_PROVIDER_PATH = ZK_REGISTRY_PATH + "/provider";
}
