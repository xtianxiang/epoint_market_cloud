package com.hmall.trade.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.common.constant.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author xu
 * @version 1.0
 * @date 2024/7/14 15:18
 * @DESCRIPTION
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDelayMessageListener {
    private final IOrderService orderService;
    private final PayClient payClient;
    @RabbitListener(queues = MQConstants.OrderMicroService.DELAY_ORDER_QUEUE_NAME)
    public void listenOrderDelayMessage(Message message) {

        log.info("接收到的消息为：{},接收时间为：{}",message,new Date());
        // 1.查询订单
        String orderId=new String(message.getBody());
        Long longorderid =  Long.getLong(orderId);
        if(StrUtil.isNotBlank(orderId)) {
            Order order = orderService.getById(orderId);
            // 2.检测订单状态，判断是否已支付
            if (order == null || order.getStatus() != 1) {
                // 订单不存在或者已经支付
                return;
            }
        }
        // 3.未支付，需要查询支付流水状态
        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(Long.getLong(orderId));
        // 4.判断是否支付
        if (payOrder != null && payOrder.getStatus() == 3) {
            // 4.1.已支付，标记订单状态为已支付
            orderService.markOrderPaySuccess(longorderid);
        } else {
            // TODO 4.2.未支付，取消订单，回复库存
            orderService.cancelOrder(orderId);
        }
    }
}
