package com.qf.shop_email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication(scanBasePackages = "com.qf")
@EnableEurekaClient
public class ShopEmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopEmailApplication.class, args);
    }

}
