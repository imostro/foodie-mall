package com.foodie.service;

import com.foodie.pojo.UserAddress;
import com.foodie.pojo.bo.AddressBO;

import java.util.List;

/**
 * @author Gray
 * @date 2020/10/24 0:25
 */
public interface UserAddressService extends BaseService {

    /**
     * 通过用户id查询用户的收货地址
     * @param userId 用户id
     * @return 用户地址列表
     */
    List<UserAddress> queryAllAddressByUserId(String userId);

    /**
     * 新增用户收货地址
     * @param addressBO 前端封装数据
     */
    void addUserAddress(AddressBO addressBO);

    /**
     * 更新用户收货地址
     * @param addressBO 前端封装数据
     */
    void updateUserAddress(AddressBO addressBO);

    /**
     * 删除用户收货地址
     * @param userId 用户id
     * @param addressId 用户地址id
     */
    void deleteUserAddress(String userId, String addressId);

    /**
     * 设置用户默认收货地址
     * @param userId 用户id
     * @param addressId 用户地址id
     */
    void setDefaultAddress(String userId, String addressId);

    /**
     * 通过用户id和地址id查询地址
     * @param userId 用户id
     * @param addressId 地址id
     * @return 用户地址
     */
    UserAddress queryUserAddress(String userId, String addressId);

}
