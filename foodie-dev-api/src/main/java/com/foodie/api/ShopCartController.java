package com.foodie.api;

import com.foodie.pojo.JSONResult;
import com.foodie.pojo.bo.ShopcartBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Gray
 * @date 2020/10/23 23:34
 */
@Api(value = "购物车接口Controller", tags = {"购物车接口相关的api"})
@RestController
@RequestMapping("shopcart")
public class ShopCartController {

    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public JSONResult add(
            @ApiParam(name = "itemId", value = "商品Id",required = true)
            @RequestParam String itemId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response){

        if (StringUtils.isEmpty(itemId)){
            return JSONResult.errorMsg("");
        }
        System.out.println(shopcartBO);

        // TODO 前端用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存

        return JSONResult.ok();
    }


    @ApiOperation(value = "删除购物车商品", notes = "删除购物车商品", httpMethod = "POST")
    @PostMapping("/del")
    public JSONResult del(
            @ApiParam(name = "userId", value = "用户Id",required = true)
            @RequestParam String userId,
            @ApiParam(name = "itemSpecId", value = "商品规格id", required = true)
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response){

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(itemSpecId)){
            return JSONResult.errorMsg("参数不能为空");
        }

        // TODO 用户在页面删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除后端购物车中的商品

        return JSONResult.ok();
    }
}
