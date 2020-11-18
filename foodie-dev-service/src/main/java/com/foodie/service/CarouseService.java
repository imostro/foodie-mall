package com.foodie.service;

import com.foodie.pojo.Carousel;

import java.util.List;

/**
 * @author Gray
 * @date 2020/10/22 18:00
 */
public interface CarouseService extends BaseService {

    /**
     * 查询所有轮播图
     * @param isShow
     * @return carousel list
     */
    List<Carousel> queryAll(Integer isShow);
}
