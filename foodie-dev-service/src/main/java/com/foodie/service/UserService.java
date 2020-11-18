package com.foodie.service;

import com.foodie.pojo.Users;

/**
 * @author Gray
 * @date 2020/10/20 9:46
 */
public interface UserService extends BaseService {

    /**
     * check username whether exist
     * @param username username
     * @return true represent exist or false represent exist
     */
    boolean queryUserNameIsExist(String username);

    /**
     * register new user
     * @param username username
     * @param password user password
     * @return user data
     */
    Users createUser(String username, String password);

    /**
     * user login
     * @param username username
     * @param password user password
     * @return user data
     */
    Users login(String username, String password);
}
