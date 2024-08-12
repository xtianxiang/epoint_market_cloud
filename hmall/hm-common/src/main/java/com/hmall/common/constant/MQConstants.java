package com.hmall.common.constant;

/**
 * @author xu
 * @version 1.0
 * @date 2024/7/14 11:56
 * @DESCRIPTION mq常量
 */
public class MQConstants {
    public static final class OrderMicroService{
        /**
         * @description:订单微服务延时交换机
         */
        public static final String DELAY_EXCHANGE_NAME = "trade.delay.direct";
        /**
         * @description:订单微服务延时队列
         */
        public static final String DELAY_ORDER_QUEUE_NAME = "trade.delay.order.queue";
        /**
         * @description:订单微服务延时路由键
         **/
        public static final String DELAY_ORDER_KEY = "delay.order.query";
        /**
         * @description:订单微服务延时时间 毫秒
         */
        public static final Long DELAY_NUM = 15000L;
    }


}
