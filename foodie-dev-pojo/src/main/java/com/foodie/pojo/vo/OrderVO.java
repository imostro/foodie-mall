package com.foodie.pojo.vo;

import com.foodie.pojo.bo.ShopcartBO;

import java.util.List;

public class OrderVO {

    private String orderId;
    private MerchantOrdersVO merchantOrdersVO;
    private List<ShopcartBO> ToBeRemovedShopcartList;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public MerchantOrdersVO getMerchantOrdersVO() {
        return merchantOrdersVO;
    }

    public void setMerchantOrdersVO(MerchantOrdersVO merchantOrdersVO) {
        this.merchantOrdersVO = merchantOrdersVO;
    }

    public List<ShopcartBO> getToBeRemovedShopcartList() {
        return ToBeRemovedShopcartList;
    }

    public void setToBeRemovedShopcartList(List<ShopcartBO> toBeRemovedShopcartList) {
        ToBeRemovedShopcartList = toBeRemovedShopcartList;
    }
}