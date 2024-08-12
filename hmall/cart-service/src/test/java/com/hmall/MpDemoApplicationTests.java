package com.hmall;

import com.hmall.cart.service.ICartService;
import com.hmall.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MpDemoApplicationTests {
@Autowired
 ICartService cartService;
    @Test
    void contextLoads() {
        cartService.queryMyCarts();
    }

}
