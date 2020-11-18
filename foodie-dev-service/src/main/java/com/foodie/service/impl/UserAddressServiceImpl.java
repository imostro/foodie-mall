package com.foodie.service.impl;

import com.foodie.enums.YesOrNo;
import com.foodie.mapper.UserAddressMapper;
import com.foodie.pojo.UserAddress;
import com.foodie.pojo.bo.AddressBO;
import com.foodie.service.UserAddressService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Gray
 * @date 2020/10/24 0:25
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private Sid sid;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<UserAddress> queryAllAddressByUserId(String userId) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);

        return userAddressMapper.select(address);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUserAddress(AddressBO addressBO) {
        int isDefault = 0;
        List<UserAddress> list = queryAllAddressByUserId(addressBO.getUserId());
        if (list == null || list.size() == 0){
            isDefault = 1;
        }

        UserAddress address = getUserAddress(addressBO);
        address.setId(sid.nextShort());
        address.setCreatedTime(new Date());
        address.setUpdatedTime(new Date());
        address.setIsDefault(isDefault);

        userAddressMapper.insert(address);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUserAddress(AddressBO addressBO) {
        UserAddress address = getUserAddress(addressBO);
        address.setUpdatedTime(new Date());

        userAddressMapper.updateByPrimaryKeySelective(address);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUserAddress(String userId, String addressId) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setId(addressId);

        userAddressMapper.delete(address);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setDefaultAddress(String userId, String addressId) {

        // 1. 先把之前默认的地址改为非默认
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setIsDefault(YesOrNo.YES.type);
        List<UserAddress> list = userAddressMapper.select(address);
        // 有可能没有设置过默认地址
        for (UserAddress userAddress : list) {
            userAddress.setIsDefault(YesOrNo.NO.type);
            userAddressMapper.updateByPrimaryKeySelective(userAddress);
        }

        // 2. 把当前改为默认
        address = new UserAddress();
        address.setUserId(userId);
        address.setId(addressId);
        address.setIsDefault((YesOrNo.YES.type));
        userAddressMapper.updateByPrimaryKeySelective(address);
    }

    @Override
    public UserAddress queryUserAddress(String userId, String addressId) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setId(addressId);
        return userAddressMapper.selectOne(address);
    }

    private UserAddress getUserAddress(AddressBO addressBO){
        UserAddress address = new UserAddress();

        address.setId(addressBO.getAddressId());
        address.setUserId(addressBO.getUserId());
        address.setCity(addressBO.getCity());
        address.setMobile(addressBO.getMobile());
        address.setDetail(addressBO.getDetail());
        address.setDistrict(addressBO.getDistrict());
        address.setProvince(addressBO.getProvince());
        address.setReceiver(addressBO.getReceiver());

        return address;
    }
}
