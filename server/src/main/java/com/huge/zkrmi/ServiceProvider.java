package com.huge.zkrmi;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;

/**
 * RMI服务提供者
 *
 * @author Sean
 * @date 2019/10/09
 */
public class ServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProvider.class);

    //用于等待SyncConnectd 事件触发后继续执行当前线程
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 发布RMI服务并注册RMI 地址
     * @param remote
     * @param host
     * @param port
     */
    public void publish(Remote remote, String host, int port){
        String url = publishService(remote, host, port);
        if (null != url){
            ZooKeeper zooKeeper = connectServer();
            if(null != zooKeeper){
                createNode(zooKeeper, url);
            }
        }
    }

    /**
     * 发布RMI服务
     * @param remote
     * @param host
     * @param port
     * @return
     */
    private String publishService(Remote remote, String host, int port){
        LOGGER.debug("start publishService");
        String url = null;
        try {
            url = String.format("rmi://%s:%d/%s", host, port, remote.getClass().getName());
            //指定服务暴露的端口号
            LocateRegistry.createRegistry(port);
            Naming.rebind(url, remote);
            LOGGER.debug("publish rmi service(url:{})", url);
        }catch (Exception e){
            LOGGER.error("", e);
        }
        return url;
    }

    /**
     * 连接ZooKeeper
     * @return
     */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
           zk = new ZooKeeper(Constant.ZK_CONNECT_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        //连接成功，唤醒线程继续执行
                        countDownLatch.countDown();
                    }
                }
            });
           countDownLatch.await();
        } catch (Exception e) {
           LOGGER.error("", e);
        }
        return zk;
    }

    private void createNode(ZooKeeper zooKeeper, String url){
        LOGGER.debug("连接成功，创建节点");
        try {
            byte[] data = url.getBytes();
            //创建一个临时性且有序的ZNode
            String path = zooKeeper.create(Constant.ZK_PROVIDER_PATH, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        }catch (Exception e){
            LOGGER.error("", e);
        }

    }
}
