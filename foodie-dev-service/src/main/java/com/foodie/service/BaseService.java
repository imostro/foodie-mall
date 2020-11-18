package com.foodie.service;

import com.foodie.pojo.PagedGridResult;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author: Gray
 * @date 2020/11/6 21:06
 */
public interface BaseService {

     default PagedGridResult setterPagedGird(List<?> list, Integer page){
        PageInfo<?> info = new PageInfo<>(list);
        PagedGridResult gird = new PagedGridResult();
        gird.setPage(page);
        gird.setRecords(info.getTotal());
        gird.setRows(list);
        gird.setTotal(info.getPages());

        return gird;
    }
}
