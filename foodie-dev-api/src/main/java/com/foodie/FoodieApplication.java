package com.foodie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author Gray
 * @date 2020/10/18 11:34
 */
@SpringBootApplication
@MapperScan("com.foodie.mapper")
@ComponentScan(basePackages =  {"com.foodie", "org.n3r.idworker"})
@EnableScheduling   // 开启定时任务
public class FoodieApplication {

    /**
     * SpringBoot如何启动
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(FoodieApplication.class);
    }
}
