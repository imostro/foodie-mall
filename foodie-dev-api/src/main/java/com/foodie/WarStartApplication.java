package com.foodie;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 打包war包 增加启动类
 * @Author: Gray
 * @date 2020/11/8 16:29
 */
public class WarStartApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // 指定application的spring boot 启动类
        return builder.sources(FoodieApplication.class);
    }
}
