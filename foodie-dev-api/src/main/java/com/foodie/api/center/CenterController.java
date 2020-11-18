package com.foodie.api.center;

import com.foodie.pojo.JSONResult;
import com.foodie.pojo.Users;
import com.foodie.service.center.CenterUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Gray
 * @date 2020/11/2 1:33
 */
@Api(value = "中心用户", tags = {"用户中心的相关接口"})
@RestController
@RequestMapping("center")
public class CenterController {

    @Autowired
    private CenterUserService centerUserService;

    @ApiOperation(value = "用户地址", notes = "用户地址", httpMethod = "GET")
    @GetMapping("userInfo")
    public JSONResult userInfo(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam  String userId){
        Users users = centerUserService.queryUserInfoByUserId(userId);
        users.setPassword(null);
        return JSONResult.ok(users);
    }

}
