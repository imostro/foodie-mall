package com.foodie.service.impl.center;

import com.foodie.mapper.UsersMapper;
import com.foodie.pojo.Users;
import com.foodie.pojo.bo.UserBO;
import com.foodie.pojo.bo.center.CenterUserBO;
import com.foodie.service.center.CenterUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Author: Gray
 * @date 2020/11/2 1:36
 */
@Service
public class CenterUserServiceImpl implements CenterUserService {

    @Autowired
    private UsersMapper usersMapper;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserInfoByUserId(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Users updateUserInfo(String userId, CenterUserBO userBO) {
        Users users = new Users();
        BeanUtils.copyProperties(userBO, users);

        users.setId(userId);
        users.setUpdatedTime(new Date());

        usersMapper.updateByPrimaryKeySelective(users);

        return queryUserInfoByUserId(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Users updateUserFace(String userId, String userFaceUrl) {
        Users users = usersMapper.selectByPrimaryKey(userId);
        users.setFace(userFaceUrl);
        users.setUpdatedTime(new Date());
        usersMapper.updateByPrimaryKeySelective(users);

        return users;
    }
}
