package com.muti.thread.concurrent_competitor.util;

import java.text.SimpleDateFormat;

/**
 * @Description:系统订单号生成规则
 * @Author: dingjl
 * @Date: 2018年4月12日
 * 订单号生成规则
 * 日期+long
 * long 0-0000000000-00000000-000000000-00000000-0000000000-0000000000-0000-00
 * 1保留位置   17位单天秒数  8位数据中心  10位服务器序号 26位订单号  2位保留位数
 **/
public class SnowflakeIdFactory {

    private final long workerIdBits = 10L;
    private final long datacenterIdBits = 8L;
    private final long holdBits = 2L;
    private final long sequenceBits = 26L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    private final long workerIdShift = sequenceBits+holdBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits+holdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits+holdBits;

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;



    public SnowflakeIdFactory(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized String  nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            //服务器时钟被调整了,ID生成器停止服务.
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
        return dateformat.format(timestamp*1000)+(((getCurrentDaySecond(timestamp)) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | (sequence<<holdBits));
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long getCurrentDaySecond(long timestamp ) {
        return (timestamp%100000);
    }

    protected long timeGen() {
        return System.currentTimeMillis()/1000;
    }


    public static void main(String[] args) {

        SnowflakeIdFactory idWorker = new SnowflakeIdFactory(1, 2);
        for(int i=0;i<1000; i++){
            System.out.println(idWorker.nextId());
        }

    }
}
