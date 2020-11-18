package com.foodie.service.center;

import com.foodie.pojo.Users;
import com.foodie.pojo.bo.UserBO;
import com.foodie.pojo.bo.center.CenterUserBO;
import com.foodie.service.BaseService;

/**
 * @Author: Gray
 * @date 2020/11/2 1:36
 */
public interface CenterUserService extends BaseService {

    /**
     * 查询用户信息
     * @param userId 用户id
     * @return user info
     */
    Users queryUserInfoByUserId(String userId);

    /**
     * 修改用户信息
     *
     * @param userId 用户id
     * @param userBO 用户传输对象
     * @return user info
     */
    Users updateUserInfo(String userId, CenterUserBO userBO);

    /**
     * 更新用户头像
     * @param userId 用户id
     * @param userFaceUrl 图片地址
     * @return users
     */
    Users updateUserFace(String userId, String userFaceUrl);
}
