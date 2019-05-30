package com.muti.thread.concurrent_competitor.factory;


import com.muti.thread.concurrent_competitor.util.SnowflakeIdFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * 单例的创建
 * @author: lph
 * @date:  2019/5/29 11:08
 * @version V1.0
 */
public class UniqueIndexServiceImpl {

    @Value(value = "${service.datacenter}")
    private  int dataCenter;
    @Value(value = "${service.serviceid}")
    private  int serviceId;

    /** volatile关键字的作用是保证对象的可见性和顺序性 .*/
    /**
     *  顺序性: 对象按照下面的步骤我进行对象的创建,否则在并发的时候可能出现对象为初始化被调用而出现异常
     * 分配内存空间。(1)
     * 初始化对象。(2)
     * 将 singleton 对象指向分配的内存地址。(3)
     */
    private static volatile SnowflakeIdFactory uniquefactory;

    public  String getUniqueIndex() {
        if (uniquefactory == null) {
            synchronized (UniqueIndexServiceImpl.class) {
                if (uniquefactory == null) {
                    uniquefactory = new SnowflakeIdFactory(serviceId,dataCenter);
                }
            }
        }
        return uniquefactory.nextId();
    }

    public static void main(String[] args){
        UniqueIndexServiceImpl uniqueIndexService = new UniqueIndexServiceImpl();
        String uniqueIndex = uniqueIndexService.getUniqueIndex();
        System.err.println(uniqueIndex);
    }
}
