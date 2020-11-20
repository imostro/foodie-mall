package com.foodie.api;

import com.foodie.enums.OrderStatusEnum;
import com.foodie.enums.PayMethod;
import com.foodie.pojo.JSONResult;
import com.foodie.pojo.OrderStatus;
import com.foodie.pojo.bo.ShopcartBO;
import com.foodie.pojo.bo.SubmitOrderBO;
import com.foodie.pojo.vo.MerchantOrdersVO;
import com.foodie.pojo.vo.OrderVO;
import com.foodie.service.OrderService;
import com.foodie.utils.CookieUtils;
import com.foodie.utils.JsonUtils;
import com.foodie.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * @author Gray
 * @date 2020/10/24 10:06
 */
@Api(value = "订单", tags = {"订单的相关接口"})
@RestController
@RequestMapping("orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisOperator redisOperator;

    // 支付中心的调用地址
    String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";

    // 微信支付成功 -> 支付中心 -> 天天吃货平台
    //                       |-> 回调通知的url
    String payReturnUrl = "http://114.116.252.186:8088/foodie-dev-api/orders/notifyMerchantOrderPaid";

    // 用户上传头像的位置
    public static final String IMAGE_USER_FACE_LOCATION = File.separator + "workspaces" +
            File.separator + "images" +
            File.separator + "foodie" +
            File.separator + "faces";

    public static final String FOODIE_SHOPCART = "shopcart";

    @ApiOperation(value = "创建商品订单", notes = "创建商品订单", httpMethod = "POST")
    @PostMapping("create")
    public JSONResult create(@RequestBody SubmitOrderBO submitOrderBO,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        JSONResult checkResult = checkSubmitOrderBO(submitOrderBO);
        if (!checkResult.isOK()) {
            return checkResult;
        }

        // 0. 解析redis中的购物车数据
        String cartKey = FOODIE_SHOPCART + ":" + submitOrderBO.getUserId();
        String cartItemsStr = redisOperator.get(cartKey);
        if (StringUtils.isEmpty(cartKey)){
            return JSONResult.errorMsg("当前购物车没有商品");
        }
        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(cartItemsStr, ShopcartBO.class);

        // 1. 创建订单
        OrderVO orderVO = orderService.createOrder(shopcartList, submitOrderBO);

        // 2. 从购物车中获取订单数据，移除redis中对应的购物车数据
        // 整合redis之后，完善购物车中的已结算商品清除，并且同步到前端的cookie
        List<ShopcartBO> toBeRemovedShopcartList = orderVO.getToBeRemovedShopcartList();
        shopcartList.removeAll(toBeRemovedShopcartList);
        String shopcartStr = JsonUtils.objectToJson(shopcartList);
        redisOperator.set(cartKey, shopcartStr);
        CookieUtils.setCookie(request, response,  FOODIE_SHOPCART, shopcartStr, true);

        // 3. 向支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);
        // 为了方便测试购买，所以所有的支付金额都统一改为1分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("imoocUserId", "imooc");
        headers.add("password", "imooc");

        ResponseEntity<JSONResult> resultEntity = restTemplate.postForEntity(paymentUrl,
                new HttpEntity<>(merchantOrdersVO, headers),
                JSONResult.class);

        JSONResult result = resultEntity.getBody();

        if (result.getStatus() != HttpStatus.OK.value()) {
            logger.error("发送错误：{}", result.getMsg());
            return JSONResult.errorMsg("支付中心订单创建失败，请联系管理员！");
        }

        // 4. 返回订单id给前端
        return JSONResult.ok(orderVO.getOrderId());
    }


    private JSONResult checkSubmitOrderBO(SubmitOrderBO submitOrderBO) {
        if (StringUtils.isEmpty(submitOrderBO.getUserId()) ||
                StringUtils.isEmpty(submitOrderBO.getAddressId()) ||
                StringUtils.isEmpty(submitOrderBO.getItemSpecIds())) {
            return JSONResult.errorMsg("相关数据不能为空");
        }

        if (StringUtils.isEmpty(submitOrderBO.getPayMethod())) {
            return JSONResult.errorMsg("支付方式不能为空");
        }

        if (!(submitOrderBO.getPayMethod().equals(PayMethod.WEIXIN.type) ||
                submitOrderBO.getPayMethod().equals(PayMethod.ALIPAY.type))) {
            return JSONResult.errorMsg("非法支付方式");
        }

        return JSONResult.ok();
    }

    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId) {
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER);

        return HttpStatus.OK.value();
    }

    @PostMapping("getPaidOrderInfo")
    public JSONResult getPaidOrderInfo(String orderId) {
        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        return JSONResult.ok(orderStatus);
    }
}
