package com.foodie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Gray
 * @date 2020/10/21 16:34
 */
@Configuration
@EnableSwagger2
public class Swagger2 {

    // 配置swagger2和兴配置 docket
    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)  // 指定api类型为swagger2
        .apiInfo(apiInfo())     // 用于定义api文档汇总
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.foodie.api"))   //指定controller包
        .paths(PathSelectors.any())     // 所有controller
        .build();
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("天天吃货 电商平台API")
                .contact(new Contact("foodie", "http://www.foodie.com",
                        "386344008@qq.com"))
                .description("天天吃货提供的api文档")
                .version("1.0.1")
                .termsOfServiceUrl("https://www.foodie.com")
                .build();
    }
}
