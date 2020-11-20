package com.foodie.api;

import com.foodie.pojo.JSONResult;
import com.foodie.pojo.Users;
import com.foodie.pojo.bo.ShopcartBO;
import com.foodie.pojo.bo.UserBO;
import com.foodie.service.UserService;
import com.foodie.utils.CookieUtils;
import com.foodie.utils.JsonUtils;
import com.foodie.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;
import sun.nio.cs.US_ASCII;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gray
 * @date 2020/10/20 10:27
 */
@Api(value = "注册登录", tags = {"用于注册登录相关的接口"})
@RestController
@RequestMapping("passport")
public class PassportController {

    public static final String FOODIE_SHOPCART = "shopcart";

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
    @GetMapping("usernameIsExist")
    public JSONResult queryUsernameIsExist(@RequestParam String username){
        if (StringUtils.isEmpty(username)){
            return JSONResult.errorMsg("用户名不能为空");
        }
        boolean exist = userService.queryUserNameIsExist(username);
        if (exist){
            return JSONResult.errorMsg("用户名已存在");
        }

        return JSONResult.ok();
    }

    @ApiOperation(value = "用户名注册", notes = "用户名注册", httpMethod = "POST")
    @PostMapping("regist")
    public JSONResult register(HttpServletRequest request, HttpServletResponse response, @RequestBody UserBO userBO){
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();

        // 1. 用户名或密码不能为空
        if (StringUtils.isEmpty(username) ||
                StringUtils.isEmpty(password) ||
                StringUtils.isEmpty(confirmPassword)){
            return JSONResult.errorMsg("用户名或密码不能为空");
        }

        // 2. 判断密码长度是否小于6
        if (password.length() < 6){
            return JSONResult.errorMsg("密码长度不能小于6");
        }

        // 3. 校验两次密码是否一致
        if (password.length() != confirmPassword.length()||
                !Objects.equals(password, confirmPassword)){
            return JSONResult.errorMsg("两次密码不一致");
        }

        // 4. 注册
        Users userResult = userService.createUser(username, password);

        // 5. 注册成功后直接登录，设置cookie
        setNullProperty(userResult);
        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);

        // TODO 生成用户token，存入redis会话
        // 同步购物车数据
        sycnShopcartData(userResult.getId(), request, response);

        return JSONResult.ok();
    }

    @ApiOperation(value = "用户名登录", notes = "用户名登录", httpMethod = "POST")
    @PostMapping("login")
    public JSONResult login(HttpServletRequest request, HttpServletResponse response, @RequestBody UserBO userBO){
        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 1. 判断用户名或密码是否为空
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return JSONResult.errorMsg("用户名或密码不能为空");
        }

        // 2. 登录验证
        Users userResult = userService.login(username, password);
        if (userResult == null){
            return JSONResult.errorMsg("账号或密码错误");
        }
        // 登录成功，设置登录的cookie
        setNullProperty(userResult);
        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);

        // TODO 生成用户token，存入redis会话
        // 同步购物车数据
        sycnShopcartData(userResult.getId(), request, response);

        return JSONResult.ok(userResult);
    }

    /**
     * 1. redis中无数据，如果cookie中的购物车为空，那么这个时候不做任何处理
     *                 如果cookie中的购物车不为空，此时直接放入redis中
     * 2. redis中有数据，如果cookie中的购物车为空，那么直接把redis的购物车覆盖本地cookie
     *                 如果cookie中的购物车不为空，
     *                      如果cookie中的某个商品在redis中存在，
     *                      则以cookie为主，删除redis中的，
     *                      把cookie中的商品直接覆盖redis中（参考京东）
     * 3. 同步到redis中去了以后，覆盖本地cookie购物车的数据，保证本地购物车的数据是同步最新的
     */
    private void sycnShopcartData(String userId, HttpServletRequest request, HttpServletResponse response){
        String cartKey = FOODIE_SHOPCART + ":" + userId;

        // redis中的购物车数据
        String redisShopCartData = redisOperator.get(cartKey);
        // cookie的购物车数据
        String cookieShopCartData = CookieUtils.getCookieValue(request, FOODIE_SHOPCART);

        if (StringUtils.isEmpty(redisShopCartData)){
            // 如果cookie中购物车数据不为空，则直接把它放入到redis中
            if (!StringUtils.isEmpty(cookieShopCartData)){
                redisOperator.set(cartKey, cookieShopCartData);
            }
        }else{
            // 如果cookie数据为空，则直接使用redis的进行覆盖
            if (StringUtils.isEmpty(cookieShopCartData)){
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, redisShopCartData);
            }else{
                /*
                 * 1. 已经存在的，把cookie中对应的数量，覆盖redis（参考京东）
                 * 2. 该项商品标记为待删除，统一放入一个待删除的list
                 * 3. 从cookie中清理所有的待删除list
                 * 4. 合并redis和cookie中的数据
                 * 5. 更新到redis和cookie中
                 */
                List<ShopcartBO> redisList = JsonUtils.jsonToList(redisShopCartData, ShopcartBO.class);
                List<ShopcartBO> cookieList = JsonUtils.jsonToList(cookieShopCartData, ShopcartBO.class);
                ArrayList<ShopcartBO> removeList = new ArrayList<>();

                // 1. 覆盖
                for (ShopcartBO redisCartItem : redisList) {
                    for (ShopcartBO cookieCartItem : cookieList) {
                        if (redisCartItem.getSpecId().equals(cookieCartItem.getSpecId())){
                            // 直接覆盖
                            redisCartItem.setBuyCounts(cookieCartItem.getBuyCounts());
                            removeList.add(cookieCartItem);
                            break;
                        }
                    }
                }

                // 2. 移除已覆盖商品
                cookieList.removeAll(removeList);

                // 3. 合并两者之间的商品
                redisList.addAll(cookieList);

                // 4. 刷新数据
                String cartItemJson = JsonUtils.objectToJson(redisList);
                redisOperator.set(cartKey, cartItemJson);
                CookieUtils.setCookie(request, response, FOODIE_SHOPCART, cartItemJson,true);
            }
        }

    }

    @ApiOperation(value = "用户名注销", notes = "用户名注销", httpMethod = "POST")
    @PostMapping("logout")
    public JSONResult logout(@RequestParam String userId, HttpServletRequest request, HttpServletResponse response){
        CookieUtils.deleteCookie(request, response, "user");
        // 用户退出登录，需要清空购物车
        CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART);
        // TODO 分布式会话中需要清空session

        return JSONResult.ok();
    }
    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

}
