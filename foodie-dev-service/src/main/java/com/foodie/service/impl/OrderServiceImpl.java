package com.foodie.service.impl;

import com.foodie.enums.OrderStatusEnum;
import com.foodie.enums.YesOrNo;
import com.foodie.mapper.OrderItemsMapper;
import com.foodie.mapper.OrderStatusMapper;
import com.foodie.mapper.OrdersMapper;
import com.foodie.mapper.OrdersMapperCustom;
import com.foodie.pojo.*;
import com.foodie.pojo.bo.SubmitOrderBO;
import com.foodie.pojo.vo.MerchantOrdersVO;
import com.foodie.pojo.vo.OrderVO;
import com.foodie.service.ItemService;
import com.foodie.service.OrderService;
import com.foodie.service.UserAddressService;
import com.foodie.utils.DateUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Gray
 * @date 2020/10/24 10:07
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private Sid sid;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrdersMapperCustom ordersMapperCustom;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private ItemService itemService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderVO createOrder(SubmitOrderBO submitOrderBO) {
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        String leftMsg = submitOrderBO.getLeftMsg();
        Integer payMethod = submitOrderBO.getPayMethod();
        String userId = submitOrderBO.getUserId();

        UserAddress userAddress = userAddressService.queryUserAddress(userId, addressId);

        int postAmount = 0;
        String orderId = sid.nextShort();

        // 1. 创建新订单
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);
        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setLeftMsg(leftMsg);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());
        newOrder.setPayMethod(payMethod);
        newOrder.setPostAmount(postAmount);
        newOrder.setReceiverMobile(userAddress.getMobile());
        newOrder.setReceiverName(userAddress.getReceiver());
        newOrder.setReceiverAddress(userAddress.getProvince() +" "
        + userAddress.getCity() + " " + userAddress.getDistrict()+" "+
                userAddress.getDetail());

        // 2. 循环根据itemSpecIds保存订单商品信息
        String[] specIdArr = itemSpecIds.split(",");
        List<ItemsSpec> specList = itemService.queryItemsSpecListBySpecIds(Arrays.asList(specIdArr));

        int realPayAmount = 0;
        int totalAmount = 0;
        // TODO 整合redis后，商品购买的数量重新从redis的购物车中获取
        int buyCounts = 1;

        for (ItemsSpec itemsSpec : specList) {
            realPayAmount += itemsSpec.getPriceDiscount() * buyCounts;
            totalAmount += itemsSpec.getPriceNormal() * buyCounts;

            // 封装orderItem
            Items items = itemService.queryItemsByItemId(itemsSpec.getItemId());
            String imgUrl = itemService.queryMainItemImgByItemsId(itemsSpec.getItemId());

            OrderItems orderItems = new OrderItems();
            orderItems.setId(sid.nextShort());
            orderItems.setItemId(itemsSpec.getItemId());
            orderItems.setItemSpecId(itemsSpec.getId());
            orderItems.setOrderId(orderId);
            orderItems.setBuyCounts(buyCounts);
            orderItems.setPrice(itemsSpec.getPriceDiscount());
            orderItems.setItemName(items.getItemName());
            orderItems.setItemSpecName(itemsSpec.getName());
            orderItems.setItemImg(imgUrl);

            // 扣除库存
            itemService.decreaseSpecStock(itemsSpec.getId(), buyCounts);
            orderItemsMapper.insert(orderItems);
        }

        newOrder.setRealPayAmount(realPayAmount);
        newOrder.setTotalAmount(totalAmount);

        // 3. 创建订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setOrderStatus(com.foodie.enums.OrderStatusEnum.WAIT_PAY.type);
        orderStatus.setCreatedTime(newOrder.getCreatedTime());

        // 4. 保存订单数据
        ordersMapper.insert(newOrder);
        orderStatusMapper.insert(orderStatus);

        // 5. 构建商户订单，传给支付中心
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realPayAmount + postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        //  构建自定义订单
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);

        return orderVO;
    }

    @Override
    public void updateOrderStatus(String merchantOrderId, OrderStatusEnum orderStatusEnum) {
        OrderStatus status = orderStatusMapper.selectByPrimaryKey(merchantOrderId);

        status.setOrderStatus(orderStatusEnum.type);
        status.setPayTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(status);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void closeOrder() {
        //  查询所有未付款订单，判断shifou超过1消失则关闭
        OrderStatus status = new OrderStatus();
        status.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> statusList = orderStatusMapper.select(status);

        // 查找超过一小时未支持的订单并关闭
        for (OrderStatus orderStatus : statusList) {
            int day = DateUtil.daysBetween(orderStatus.getCreatedTime(), new Date());
            // 关闭操作
            if (day >= 1){
                doCloseOrder(orderStatus.getOrderId());
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void doCloseOrder(String orderId){
        OrderStatus status = new OrderStatus();
        status.setOrderId(orderId);
        status.setOrderStatus(OrderStatusEnum.CLOSE.type);
        orderStatusMapper.updateByPrimaryKeySelective(status);
    }
}
