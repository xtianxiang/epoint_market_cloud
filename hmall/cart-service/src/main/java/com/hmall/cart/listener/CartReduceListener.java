package com.hmall.cart.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hmall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xu
 * @version 1.0
 * @date 2024/7/13 22:51
 * @DESCRIPTION
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartReduceListener {
    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic"),
            key = "order.create"
    ))
    public void listenPaySuccess(Message message){
        log.info("-----------------------message-----------------:"+message);
       if(message!=null){
           String body = message.getBody().toString();
           if(StrUtil.isNotBlank(body)) {
               JSONObject jsonObject = JSONObject.parseObject(body);
               String user =null;
                    if(jsonObject.containsKey("user")) {
                        user = jsonObject.getString("user");
                    }
               Set<Long> itemIds = new HashSet<>();
                    if(jsonObject.containsKey("itemIds")){
                        JSONArray ids = jsonObject.getJSONArray("itemIds");
                         itemIds.addAll(ids.toJavaList(Long.class));
                    }

               cartService.removeByItemIdsAndUser(user,itemIds);
           }
       }
    }
}
