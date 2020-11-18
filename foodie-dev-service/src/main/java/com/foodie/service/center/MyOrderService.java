package com.foodie.service.center;

import com.foodie.pojo.OrderStatus;
import com.foodie.pojo.Orders;
import com.foodie.pojo.PagedGridResult;
import com.foodie.pojo.vo.OrderStatusCountsVO;
import com.foodie.service.BaseService;

/**
 * @Author: Gray
 * @date 2020/11/4 20:56
 */
public interface MyOrderService extends BaseService {

    /**
     * 查询用户订单列表
     * @param userId 用户id
     * @param orderStatus 订单状态
     * @param page 页
     * @param size 每页条目
     * @return
     */
    PagedGridResult queryMyOrders(String userId,
                                  Integer orderStatus,
                                  Integer page,
                                  Integer size);

    /**
     * 商品发货
     * @param orderId
     */
    boolean updateDeliverOrderStatus(String orderId);


    /**
     * 确认收货
     * @param orderId 订单id
     */
    boolean updateComfireReceiveOrderStatus(String orderId);

    /**
     * 查询订单状态
     * @param orderId 订单id
     * @return 订单状态
     */
    OrderStatus queryOrderStatus(String orderId);

    /**
     * 删除订单
     * @param userId 用户id
     * @param orderId 订单id
     */
    boolean deleteOrder(String userId, String orderId);

    /**
     * 查询订单
     * @param userId 用户id
     * @param orderId 订单id
     * @return
     */
    Orders queryMyOrders(String userId, String orderId);

    /**
     * 查询用户各状态订单数
     * @param userId 用户id
     * @return
     */
    public OrderStatusCountsVO getOrderStatusCount(String userId);

    /**
     * 查询用户订单动向
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryOrderTrend(String userId, Integer page, Integer pageSize);
}
