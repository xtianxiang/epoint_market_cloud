package com.hmall.trade.mqconfig;


import com.hmall.common.constant.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TradeRabbitConfig {

    private final String  exchangeName = MQConstants.OrderMicroService.DELAY_EXCHANGE_NAME;

    private final String queueDelayName = MQConstants.OrderMicroService.DELAY_ORDER_QUEUE_NAME;

    /**
     * 创建自定义交换机
     * @return
     */
    @Bean
    public CustomExchange customExchange() {
        Map<String, Object> arguments =new HashMap<>();
        arguments.put("x-delayed-type","direct"); //放一个参数
//        CustomExchange(String name, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        return  new CustomExchange(exchangeName,"x-delayed-message",true,false,arguments);

    }

    @Bean
    public Queue queue() {
        //方式2 建造者
        return QueueBuilder
                .durable(queueDelayName) //队列名称
                .build();
    }

    @Bean
    public Binding binding(CustomExchange customExchange, Queue queue) {
        //绑定，也指定路由key，加noargs 方法
        return BindingBuilder.bind(queue).to(customExchange).with(MQConstants.OrderMicroService.DELAY_ORDER_KEY).noargs();
    }

}
