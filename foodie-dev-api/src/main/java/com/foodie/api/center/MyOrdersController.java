package com.foodie.api.center;

import com.foodie.enums.OrderStatusEnum;
import com.foodie.pojo.JSONResult;
import com.foodie.pojo.OrderStatus;
import com.foodie.pojo.PagedGridResult;
import com.foodie.pojo.vo.OrderStatusCountsVO;
import com.foodie.service.center.MyOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Gray
 * @date 2020/11/4 21:11
 */
@Api(value = "用户订单", tags = {"用户订单的相关接口"})
@RestController
@RequestMapping("myorders")
public class MyOrdersController {

    @Autowired
    private MyOrderService myOrderService;

    @ApiOperation(value = "查询用户订单列表", notes = "查询用户订单列表", httpMethod = "GET")
    @PostMapping("query")
    public JSONResult query(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderStatus", value = "订单状态", required = true)
            @RequestParam Integer orderStatus,
            @ApiParam(name = "page", value = "查询页", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam(name = "pageSize", value = "记录数", required = false)
            @RequestParam(required = false, defaultValue = "5") Integer pageSize){

        PagedGridResult result = myOrderService.queryMyOrders(userId, orderStatus, page, pageSize);
        return JSONResult.ok(result);
    }

    @ApiOperation(value = "商品发货", notes = "商品发货", httpMethod = "POST")
    @PostMapping("deliver")
    public JSONResult deliver(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId){
        OrderStatus status = myOrderService.queryOrderStatus(orderId);
        if (status == null ||
                !status.getOrderStatus().equals(OrderStatusEnum.WAIT_DELIVER.type)){
            return JSONResult.errorMsg("订单不存在或者订单已发货");
        }
        boolean update = myOrderService.updateDeliverOrderStatus(orderId);

        if (!update){
            return JSONResult.errorMsg("订单更新失败");
        }
        return JSONResult.ok();
    }

    @ApiOperation(value = "确认收货", notes = "确认收货", httpMethod = "POST")
    @PostMapping("confirmReceive")
    public JSONResult confirmReceive(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId){
        OrderStatus status = myOrderService.queryOrderStatus(orderId);
        if (status == null ||
                !status.getOrderStatus().equals(OrderStatusEnum.WAIT_RECEIVE.type)){
            return JSONResult.errorMsg("订单不存在或者订单已收货");
        }
        boolean update = myOrderService.updateComfireReceiveOrderStatus(orderId);
        if (!update){
            return JSONResult.errorMsg("订单更新失败");
        }
        return JSONResult.ok();
    }

    @ApiOperation(value = "删除订单", notes = "删除订单", httpMethod = "POST")
    @PostMapping("deleteOrder")
    public JSONResult deleteOrder(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId){
        OrderStatus status = myOrderService.queryOrderStatus(orderId);
        if (status == null ||
                !status.getOrderStatus().equals(OrderStatusEnum.WAIT_RECEIVE.type)){
            return JSONResult.errorMsg("订单不存在或者订单已收货");
        }
        boolean update = myOrderService.deleteOrder(userId, orderId);
        if (!update){
            return JSONResult.errorMsg("订单更新失败");
        }
        return JSONResult.ok();
    }

    @PostMapping("statusCounts")
    public JSONResult statusCounts(@ApiParam(name = "userId", value = "用户id", required = true)
                                       @RequestParam String userId){
        OrderStatusCountsVO count = myOrderService.getOrderStatusCount(userId);

        return JSONResult.ok(count);

    }



    @ApiOperation(value = "订单动向", notes = "订单动向", httpMethod = "POST")
    @PostMapping("trend")
    public JSONResult trend(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "page", value = "查询页", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam(name = "pageSize", value = "记录数", required = false)
            @RequestParam(required = false, defaultValue = "5") Integer pageSize){

        PagedGridResult result = myOrderService.queryOrderTrend(userId, page, pageSize);
        return JSONResult.ok(result);
    }


}
