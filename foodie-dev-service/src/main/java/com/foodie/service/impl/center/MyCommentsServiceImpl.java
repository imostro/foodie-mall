package com.foodie.service.impl.center;

import com.foodie.enums.YesOrNo;
import com.foodie.mapper.*;
import com.foodie.pojo.OrderItems;
import com.foodie.pojo.OrderStatus;
import com.foodie.pojo.Orders;
import com.foodie.pojo.PagedGridResult;
import com.foodie.pojo.bo.center.OrderItemsCommentBO;
import com.foodie.pojo.vo.MyCommentVO;
import com.foodie.service.center.MyCommentsService;
import com.github.pagehelper.PageHelper;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Gray
 * @date 2020/11/4 22:46
 */
@Service
public class MyCommentsServiceImpl implements MyCommentsService {

    @Autowired
    private Sid sid;

    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;

    @Autowired
    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<OrderItems> queryPendingComment(String orderId) {
        OrderItems orderItems = new OrderItems();
        orderItems.setOrderId(orderId);
        List<OrderItems> items = orderItemsMapper.select(orderItems);
        return items;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveCommentList(String userId, String orderId, List<OrderItemsCommentBO> commentList) {

        // 1. 保存评论
        HashMap<String, Object> map = new HashMap<>();
        for (OrderItemsCommentBO comment : commentList) {
            comment.setCommentId(sid.nextShort());
        }
        map.put("userId", userId);
        map.put("commentList", commentList);

        itemsCommentsMapperCustom.saveComments(map);

        // 2.更新订单评论时间
        OrderStatus status = new OrderStatus();
        status.setOrderId(orderId);
        status.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(status);

        // 3. 设置订单已评论
        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setUserId(userId);
        orders.setIsComment(YesOrNo.YES.type);
        orders.setUpdatedTime(new Date());
        ordersMapper.updateByPrimaryKeySelective(orders);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public PagedGridResult queryComment(String userId, int page, int size) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        PageHelper.startPage(page, size);
        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);

        return setterPagedGird(list, page);
    }

}

