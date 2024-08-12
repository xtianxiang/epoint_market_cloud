package com.hmall.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xu
 * @version 1.0
 * @date 2024/7/8 16:13
 * @DESCRIPTION
 */
@MapperScan("com.hmall.search.mapper")
@SpringBootApplication
//@EnableDiscoveryClient
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);

        }
}
