package com.integrity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 啓動類
 * @author liuxiaoguang
 * @date 2021/7/8 16:55
 * @version 1.0
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@Configuration

public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("------------------------- Alm-Gateway 项目启动成功！！ ------------------------- ");
    }
}


