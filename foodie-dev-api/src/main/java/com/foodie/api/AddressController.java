package com.foodie.api;

import com.foodie.pojo.JSONResult;
import com.foodie.pojo.UserAddress;
import com.foodie.pojo.bo.AddressBO;
import com.foodie.service.UserAddressService;;
import com.foodie.utils.MobileEmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;



import java.util.List;

/**
 * @author Gray
 * @date 2020/10/24 0:24
 */
@Api(value = "用户地址", tags = {"用户地址的相关接口"})
@RestController
@RequestMapping("address")
public class AddressController {

    @Autowired
    private UserAddressService userAddressService;

    @PostMapping("/list")
    @ApiOperation(value = "用户地址", notes = "用户地址", httpMethod = "POST")
    public JSONResult list(@ApiParam(name = "userId", value = "用户id", required = true)
                               @RequestParam String userId){

        if (StringUtils.isEmpty(userId)){
            return JSONResult.errorMsg("用户id不能为空");
        }

        List<UserAddress> list = userAddressService.queryAllAddressByUserId(userId);
        return JSONResult.ok(list);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增用户地址", notes = "新增用户地址", httpMethod = "POST")
    public JSONResult add(@ApiParam(name = "addressBO", value = "用户收货地址", required = true)
                           @RequestBody AddressBO addressBO){
        JSONResult jsonResult = checkAddress(addressBO);
        if (jsonResult.getStatus() != 200){
            return jsonResult;
        }

        userAddressService.addUserAddress(addressBO);
        return JSONResult.ok();
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新用户地址", notes = "更新用户地址", httpMethod = "POST")
    public JSONResult update(@ApiParam(name = "addressBO", value = "用户收货地址", required = true)
                          @RequestBody AddressBO addressBO){
        JSONResult jsonResult = checkAddress(addressBO);
        if (jsonResult.getStatus() != 200){
            return jsonResult;
        }

        userAddressService.updateUserAddress(addressBO);
        return JSONResult.ok();
    }

    @PostMapping("/delete")
    @ApiOperation(value = "更新用户地址", notes = "更新用户地址", httpMethod = "POST")
    public JSONResult del(@ApiParam(name = "userId", value = "用户id", required = true)
                              @RequestParam String userId,
                                          @ApiParam(name = "addressId", value = "地址id", required = true)
                              @RequestParam String addressId){

        userAddressService.deleteUserAddress(userId, addressId);
        return JSONResult.ok();
    }

    @PostMapping("/setDefalut")
    @ApiOperation(value = "设置用户默认地址", notes = "设置用户默认地址", httpMethod = "POST")
    public JSONResult setDefalut(@ApiParam(name = "userId", value = "用户id", required = true)
                          @RequestParam String userId,
                                                 @ApiParam(name = "addressId", value = "地址id", required = true)
                          @RequestParam String addressId){

        userAddressService.setDefaultAddress(userId, addressId);
        return JSONResult.ok();
    }

    private JSONResult checkAddress(AddressBO addressBO) {
        String receiver = addressBO.getReceiver();
        if (StringUtils.isEmpty(receiver)) {
            return JSONResult.errorMsg("收货人不能为空");
        }
        if (receiver.length() > 12) {
            return JSONResult.errorMsg("收货人姓名不能太长");
        }

        String mobile = addressBO.getMobile();
        if (StringUtils.isEmpty(mobile)) {
            return JSONResult.errorMsg("收货人手机号不能为空");
        }
        if (mobile.length() != 11) {
            return JSONResult.errorMsg("收货人手机号长度不正确");
        }
        boolean isMobileOk = MobileEmailUtils.checkMobileIsOk(mobile);
        if (!isMobileOk) {
            return JSONResult.errorMsg("收货人手机号格式不正确");
        }

        String province = addressBO.getProvince();
        String city = addressBO.getCity();
        String district = addressBO.getDistrict();
        String detail = addressBO.getDetail();
        if (StringUtils.isEmpty(province) ||
                StringUtils.isEmpty(city) ||
                StringUtils.isEmpty(district) ||
                StringUtils.isEmpty(detail)) {
            return JSONResult.errorMsg("收货地址信息不能为空");
        }

        return JSONResult.ok();
    }
}
