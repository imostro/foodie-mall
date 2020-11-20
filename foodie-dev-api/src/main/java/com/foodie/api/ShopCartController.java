package com.foodie.api;

import com.foodie.pojo.JSONResult;
import com.foodie.pojo.bo.ShopcartBO;
import com.foodie.utils.CookieUtils;
import com.foodie.utils.JsonUtils;
import com.foodie.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gray
 * @date 2020/10/23 23:34
 */
@Api(value = "购物车接口Controller", tags = {"购物车接口相关的api"})
@RestController
@RequestMapping("shopcart")
public class ShopCartController {

    public static final String FOODIE_SHOPCART = "shopcart";

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public JSONResult add(
            @RequestParam String userId,
            @ApiParam(name = "itemId", value = "商品Id",required = true)
            @RequestParam String itemId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response){

        if (StringUtils.isEmpty(itemId)){
            return JSONResult.errorMsg("");
        }

        // 将商品添加到缓存
        String cartKey = FOODIE_SHOPCART + ":" + userId;
        String itemsStr = redisOperator.get(cartKey);

        List<ShopcartBO> list;
        if (StringUtils.isEmpty(itemsStr)){
            list =new ArrayList<>();
            list.add(shopcartBO);
        }else{
            list = JsonUtils.jsonToList(itemsStr, ShopcartBO.class);
            boolean ise = false;
            for (ShopcartBO itemBO : list) {
                if (shopcartBO.getSpecId().equals(itemBO.getSpecId())){
                    itemBO.setBuyCounts(itemBO.getBuyCounts() + shopcartBO.getBuyCounts());
                    ise = true;
                    break;
                }
            }
            if (!ise){
                list.add(shopcartBO);
            }
        }
        // 添加或覆盖redis中的购物车商品
        String listJson = JsonUtils.objectToJson(list);
        redisOperator.set(cartKey, listJson);
        // 更新客户端cookie
        CookieUtils.setCookie(request, response, FOODIE_SHOPCART, listJson);
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

        // 用户在页面删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除后端购物车中的商品
        String cartKey = FOODIE_SHOPCART + ":" + userId;
        String itemsStr = redisOperator.get(cartKey);
        if (StringUtils.isEmpty(itemsStr)){
            return JSONResult.errorMsg("商品不存在于购物车");
        }

        List<ShopcartBO> list = JsonUtils.jsonToList(itemsStr, ShopcartBO.class);
        Iterator<ShopcartBO> iterator = list.iterator();
        while (iterator.hasNext()){
            ShopcartBO bo = iterator.next();
            if (itemSpecId.equals(bo.getSpecId())){
                iterator.remove();
                break;
            }
        }
        String listJson = JsonUtils.objectToJson(list);
        redisOperator.set(cartKey, listJson);
        // 覆盖cookie
        CookieUtils.setCookie(request, response, FOODIE_SHOPCART, listJson);

        return JSONResult.ok();
    }
}
