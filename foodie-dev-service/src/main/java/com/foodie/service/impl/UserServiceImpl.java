package com.foodie.service.impl;

import com.foodie.enums.Sex;
import com.foodie.mapper.UsersMapper;
import com.foodie.pojo.Users;
import com.foodie.service.UserService;
import com.foodie.utils.DateUtil;
import com.foodie.utils.MD5Utils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

/**
 *
 * @author Gray
 * @date 2020/10/20 9:46
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String USER_FACE = "http://122.152.205.72:88/group1/M00/00/05/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean queryUserNameIsExist(String username) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        Users users = usersMapper.selectOneByExample(example);
        return users != null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users createUser(String username, String password) {
        try {
            Users user = new Users();

            // 分布式id
            String userId = sid.nextShort();
            user.setId(userId);
            // 设置用户名和密码
            user.setUsername(username);
            user.setPassword(MD5Utils.getMD5Str(password));
            // 默认用户昵称同用户名
            user.setNickname(username);
            // 默认头像
            user.setFace(USER_FACE);
            // 默认生日
            user.setBirthday(DateUtil.stringToDate("1900-01-01"));
            // 默认性别为 保密
            user.setSex(Sex.secret.type);
            user.setCreatedTime(new Date());
            user.setUpdatedTime(new Date());

            usersMapper.insert(user);
            setNullProperty(user);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Users login(String username, String password) {
        try {
            Users users = new Users();
            users.setUsername(username);
            users.setPassword(MD5Utils.getMD5Str(password));
            Users user = usersMapper.selectOne(users);
            setNullProperty(user);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private void setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
    }
}
