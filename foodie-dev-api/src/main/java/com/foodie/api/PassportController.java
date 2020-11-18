package com.foodie.api;

import com.foodie.pojo.JSONResult;
import com.foodie.pojo.Users;
import com.foodie.pojo.bo.UserBO;
import com.foodie.service.UserService;
import com.foodie.utils.CookieUtils;
import com.foodie.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author Gray
 * @date 2020/10/20 10:27
 */
@Api(value = "注册登录", tags = {"用于注册登录相关的接口"})
@RestController
@RequestMapping("passport")
public class PassportController {

    @Autowired
    private UserService userService;

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

        return JSONResult.ok(userResult);
    }

    @ApiOperation(value = "用户名注销", notes = "用户名注销", httpMethod = "POST")
    @PostMapping("logout")
    public JSONResult logout(@RequestParam String userId, HttpServletRequest request, HttpServletResponse response){
        CookieUtils.deleteCookie(request, response, "user");

        // TODO 用户退出登录，需要清空购物车
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
