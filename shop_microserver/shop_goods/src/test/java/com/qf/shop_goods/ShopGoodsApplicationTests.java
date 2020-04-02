package com.qf.shop_goods;

import com.qf.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class ShopGoodsApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(DateUtil.date2String(new Date(), "yyMMddHH"));
    }

}
