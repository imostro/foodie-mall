package com.foodie.service;

import com.foodie.enums.OrderStatusEnum;
import com.foodie.pojo.OrderStatus;
import com.foodie.pojo.bo.ShopcartBO;
import com.foodie.pojo.bo.SubmitOrderBO;
import com.foodie.pojo.vo.OrderVO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Gray
 * @date 2020/10/24 10:07
 */
public interface OrderService extends BaseService {

    /**
     * 创建订单
     *
     * @param shopcartList
     * @param submitOrderBO 订单提交信息
     * @return orderId
     */
    OrderVO createOrder(List<ShopcartBO> shopcartList, SubmitOrderBO submitOrderBO);


    /**
     * 通过订单Id修改订单状态
     * @param merchantOrderId 买家订单Id
     * @param orderStatusEnum 订单状态
     */
    void updateOrderStatus(String merchantOrderId, OrderStatusEnum orderStatusEnum);

    /**
     * 根据订单id查询订单状态信息
     * @param orderId
     * @return
     */
    OrderStatus queryOrderStatusInfo(String orderId);

    void closeOrder();

    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId);
}
