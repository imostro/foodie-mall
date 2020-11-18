package com.foodie.api.center;

import com.foodie.enums.YesOrNo;
import com.foodie.pojo.JSONResult;
import com.foodie.pojo.OrderItems;
import com.foodie.pojo.Orders;
import com.foodie.pojo.PagedGridResult;
import com.foodie.pojo.bo.center.OrderItemsCommentBO;
import com.foodie.pojo.vo.ItemCommentVO;
import com.foodie.service.center.MyCommentsService;
import com.foodie.service.center.MyOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Gray
 * @date 2020/11/4 22:44
 */
@Api(value = "用户商品评价", tags = {"用户商品评价的相关接口"})
@RestController
@RequestMapping("mycomments")
public class MyCommentsController {

    @Autowired
    private MyOrderService myOrderService;

    @Autowired
    private MyCommentsService myCommentsService;

    @ApiOperation(value = "追加商品评论", notes = "追加商品评论", httpMethod = "POST")
    @PostMapping("pending")
    public JSONResult pending(@ApiParam(name = "orderId", value = "订单id", required = true)
                                    @RequestParam String orderId,
                              @ApiParam(name = "userId", value = "用户id", required = true)
                                    @RequestParam  String userId){
        Orders orders = myOrderService.queryMyOrders(userId, orderId);
        if (orders == null || orders.getIsComment().equals(YesOrNo.YES.type)){
            return JSONResult.errorMsg("订单不存在或已经评论");
        }
        List<OrderItems> items = myCommentsService.queryPendingComment(orderId);

        return JSONResult.ok(items);
    }

    @ApiOperation(value = "保存商品评论", notes = "保存商品评论", httpMethod = "POST")
    @PostMapping("saveList")
    public JSONResult saveCommentList(@ApiParam(name = "orderId", value = "订单id", required = true)
                                @RequestParam String orderId,
                              @ApiParam(name = "userId", value = "用户id", required = true)
                                @RequestParam  String userId,
                              @ApiParam(name = "评论列表", value = "评论列表", required = true)
                                @RequestBody List<OrderItemsCommentBO> commentBOList){
        JSONResult checkResult = checkUserOrder(userId, orderId);
        if (!checkResult.isOK()){
            return checkResult;
        }
        myCommentsService.saveCommentList(userId, orderId, commentBOList);

        return JSONResult.ok();
    }

    @ApiOperation(value = "查询用户商品评论", notes = "查询用户商品评论", httpMethod = "POST")
    @PostMapping("query")
    public JSONResult query(@ApiParam(name = "userId", value = "用户id", required = true)
                                @RequestParam String userId,
                            @ApiParam(name = "page", value = "查询页", required = false)
                                @RequestParam(required = false, defaultValue = "1") Integer page,
                            @ApiParam(name = "pageSize", value = "记录数", required = false)
                                @RequestParam(required = false, defaultValue = "5") Integer pageSize){

        PagedGridResult result = myCommentsService.queryComment(userId, page, pageSize);

        return JSONResult.ok(result);
    }

    /**
     * 用于验证用户和订单是否有关联关系，避免非法用户调用
     * @return
     */
    public JSONResult checkUserOrder(String userId, String orderId) {
        Orders order = myOrderService.queryMyOrders(userId, orderId);
        if (order == null) {
            return JSONResult.errorMsg("订单不存在！");
        }
        return JSONResult.ok(order);
    }
}
