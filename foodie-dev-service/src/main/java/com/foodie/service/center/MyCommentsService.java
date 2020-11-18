package com.foodie.service.center;

import com.foodie.pojo.OrderItems;
import com.foodie.pojo.PagedGridResult;
import com.foodie.pojo.bo.center.OrderItemsCommentBO;
import com.foodie.service.BaseService;

import java.util.List;

/**
 * @Author: Gray
 * @date 2020/11/4 22:46
 */
public interface MyCommentsService extends BaseService {

    /**
     * 查询待评价商品
     *
     * @param orderId
     * @return
     */
    List<OrderItems> queryPendingComment(String orderId);

    /**
     * 保存商品评论
     *
     * @param userId
     * @param orderId
     * @param commentBOList
     */
    void saveCommentList(String userId, String orderId, List<OrderItemsCommentBO> commentBOList);

    /**
     * 查询用户评论
     * @param userId
     * @param page
     * @param size
     * @return
     */
    PagedGridResult queryComment(String userId, int page, int size);
}
