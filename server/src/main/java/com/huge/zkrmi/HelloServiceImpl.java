package com.huge.zkrmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * 发布的服务
 *
 * @author Sean
 * @date 2019/10/09
 */
public class HelloServiceImpl extends UnicastRemoteObject implements HelloService {

    public HelloServiceImpl() throws RemoteException {
        super();
    }
    public String sayHello(String name) {
        return "hello:" + name;
    }
}
