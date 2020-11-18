package com.foodie.service.impl.center;

import com.foodie.enums.OrderStatusEnum;
import com.foodie.enums.YesOrNo;
import com.foodie.mapper.OrderStatusMapper;
import com.foodie.mapper.OrdersMapper;
import com.foodie.mapper.OrdersMapperCustom;
import com.foodie.pojo.OrderStatus;
import com.foodie.pojo.Orders;
import com.foodie.pojo.PagedGridResult;
import com.foodie.pojo.vo.MyOrdersVO;
import com.foodie.pojo.vo.OrderStatusCountsVO;
import com.foodie.service.center.MyOrderService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Gray
 * @date 2020/11/4 20:56
 */
@Service
public class MyOrderServiceImpl implements MyOrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrdersMapperCustom ordersMapperCustom;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public PagedGridResult queryMyOrders(String userId, Integer orderStatus, Integer page, Integer size) {
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("userId", userId);
        if (orderStatus != null){
            map.put("orderStatus", orderStatus);
        }

        PageHelper.startPage(page, size);
        List<MyOrdersVO> list = ordersMapperCustom.queryMyOrders(map);

        return setterPagedGird(list, page);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateDeliverOrderStatus(String orderId) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setOrderStatus(OrderStatusEnum.WAIT_RECEIVE.type);
        orderStatus.setDeliverTime(new Date());

        Example example = new Example(OrderStatus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", orderId);
        criteria.andEqualTo("orderStatus", OrderStatusEnum.WAIT_DELIVER.type);

        int i = orderStatusMapper.updateByExampleSelective(orderStatus, example);
        return i>0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateComfireReceiveOrderStatus(String orderId) {
        OrderStatus status = new OrderStatus();
        status.setOrderStatus(OrderStatusEnum.SUCCESS.type);
        status.setSuccessTime(new Date());

        Example example = new Example(OrderStatus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", orderId);
        criteria.andEqualTo("orderStatus", OrderStatusEnum.WAIT_RECEIVE.type);

        int i = orderStatusMapper.updateByExampleSelective(status, example);
        return  i>0;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public OrderStatus queryOrderStatus(String orderId){
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean deleteOrder(String userId, String orderId) {
        Orders orders = new Orders();
        orders.setUpdatedTime(new Date());
        orders.setIsDelete(YesOrNo.YES.type);

        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", orderId);
        criteria.andEqualTo("userId", userId);

        int i = ordersMapper.updateByExampleSelective(orders, example);
        return i > 0;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Orders queryMyOrders(String userId, String orderId) {
        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setId(orderId);

        return ordersMapper.selectOne(orders);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public OrderStatusCountsVO getOrderStatusCount(String userId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("orderStatus", OrderStatusEnum.WAIT_PAY.type);
        int waitPayCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        map.put("orderStatus", OrderStatusEnum.WAIT_DELIVER.type);
        int waitDeliverCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        map.put("orderStatus", OrderStatusEnum.WAIT_RECEIVE.type);
        int waitReceiveCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        map.put("orderStatus", OrderStatusEnum.SUCCESS.type);
        map.put("isComment", YesOrNo.NO.type);
        int waitCommentCounts = ordersMapperCustom.getMyOrderStatusCounts(map);

        OrderStatusCountsVO countsVO = new OrderStatusCountsVO(waitPayCounts, waitDeliverCounts, waitReceiveCounts, waitCommentCounts);

        return countsVO;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public PagedGridResult queryOrderTrend(String userId, Integer page, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        PageHelper.startPage(page, pageSize);
        List<OrderStatus> list = ordersMapperCustom.getMyOrderTrend(map);

        return setterPagedGird(list, page);
    }

}
