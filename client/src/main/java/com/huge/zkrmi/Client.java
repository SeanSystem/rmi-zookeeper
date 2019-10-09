package com.huge.zkrmi;

import org.apache.log4j.PropertyConfigurator;

import java.rmi.Remote;

/**
 * RMI客户端
 *
 * @author Sean
 * @date 2019/10/09
 */
public class Client {

    public static void main(String[] args) throws InterruptedException {
        PropertyConfigurator.configure("F:\\github\\rmizookeeper\\client\\src\\main\\resources\\log4j.properties");
        ServiceConsumer consumer = new ServiceConsumer();
        while (true){
            HelloService helloService = consumer.lookUp();
            try {
               String result =  helloService.sayHello("jack");
                System.out.println(result);
            }catch (Exception e){
                e.printStackTrace();
            }
            Thread.sleep(3000);
        }
    }
}
