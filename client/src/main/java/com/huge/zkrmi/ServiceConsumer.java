package com.huge.zkrmi;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.ConnectException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;


/**
 * RMI服务消费者
 *
 * @author Sean
 * @date 2019/10/09
 */
public class ServiceConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConsumer.class);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private volatile List<String> urlList = new ArrayList<String>();

    public ServiceConsumer() {
        ZooKeeper zooKeeper = connectServer();
        if (null != zooKeeper) {
            watchNode(zooKeeper);
        }
    }

    /**
     * 观察/registry 节点下所有的子节点是否有变化
     * @param zooKeeper
     */
    private void watchNode(final ZooKeeper zooKeeper) {
        try {
            List<String> nodeList = zooKeeper.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zooKeeper);
                    }
                }
            });
            //用于存放/registry所有节点
            List<String> dataList = new ArrayList<String>();
            for(String node : nodeList){
                // 获取 /registry 的子节点中的数据
                byte[] data = zooKeeper.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));
            }
            LOGGER.debug("node data:{}", dataList);
           //更新最新的RMI地址
            urlList = dataList;
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 连接ZooKeeper
     *
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(Constant.ZK_CONNECT_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
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

    /**
     * 查找RMI服务
     * @param <T>
     * @return
     */
    public <T extends Remote> T lookUp(){
        T service = null;
        int size = urlList.size();
        if(size > 0){
            String url;
            if(size == 1){
                //若只有一个地址
                url = urlList.get(0);
                LOGGER.debug("using only url:{}", url);
            }else {
                url = urlList.get(new Random().nextInt(size));
                LOGGER.debug("using random url:{}", url);
            }
            service = lookUpService(url);
        }

        return service;
    }

    /**
     * 在JNDI中查找RMI远程服务对象
     * @param url
     * @param <T>
     * @return
     */
    private <T> T lookUpService(String url) {
        T remote = null;
        LOGGER.debug("连接的url:{}",url);
        try {
            remote = (T)Naming.lookup(url);
        } catch (Exception e) {
            if(e instanceof ConnectException){
                LOGGER.error("ConnectException -> url:{}", url);
                if(urlList.size() != 0){
                    url = urlList.get(0);
                    return lookUpService(url);
                }
            }
            LOGGER.error("", e);
        }
        return remote;
    }
}
