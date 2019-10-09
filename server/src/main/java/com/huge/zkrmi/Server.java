package com.huge.zkrmi;

import org.apache.log4j.PropertyConfigurator;
import java.rmi.RemoteException;

/**
 * 服务发布
 *
 * @author Sean
 * @date 2019/10/09
 */
public class Server {

    public static void main(String[] args) throws InterruptedException {
        PropertyConfigurator.configure("F:\\github\\rmizookeeper\\server\\src\\main\\resources\\log4j.properties");
        try {
            ServiceProvider provider = new ServiceProvider();
            HelloService helloService = new HelloServiceImpl();
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            provider.publish(helloService, host, port);
            Thread.sleep(Long.MAX_VALUE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
