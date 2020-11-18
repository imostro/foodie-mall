package com.foodie.service;

import com.foodie.pojo.Category;
import com.foodie.pojo.vo.CategoryVO;
import com.foodie.pojo.vo.NewItemsVO;

import java.util.List;
import java.util.Map;

/**
 * @author Gray
 * @date 2020/10/22 22:40
 */
public interface CategoryService extends BaseService {

    /**
     * 通过父id查看分类
     * @param rootCatId parent id
     * @return category list
     */
    List<CategoryVO> queryCategoryByParentId(Integer rootCatId);

    /**
     * 查询所有根分类
     * @return 根分类
     */
    List<Category> queryAllRootCategory();

    /**
     * 查询分类id下的6条商品数据信息
     * @param rootCatId 分类id
     * @return 商品列表
     */
    List<NewItemsVO> querySixNewItemLazy(Integer rootCatId);
}
