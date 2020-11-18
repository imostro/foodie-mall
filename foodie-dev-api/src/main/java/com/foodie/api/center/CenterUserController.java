package com.foodie.api.center;

import com.foodie.pojo.JSONResult;
import com.foodie.pojo.Users;
import com.foodie.pojo.bo.center.CenterUserBO;
import com.foodie.properties.FileUpload;
import com.foodie.service.center.CenterUserService;
import com.foodie.utils.CookieUtils;
import com.foodie.utils.DateUtil;
import com.foodie.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Gray
 * @date 2020/11/2 22:59
 */
@Api(value = "中心用户", tags = {"用户中心的相关接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController {

//    public static final String IMAGE_USER_FACE_LOCATION = "G:/imoocjava/work/project/resource/faces";

    @Autowired
    private FileUpload fileUpload;

    @Autowired
    private CenterUserService centerUserService;

    @ApiOperation(value = "更新用户信息", notes = "更新用户信息", httpMethod = "POST")
    @PostMapping("update")
    public JSONResult update(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam  String userId,
            @ApiParam(name = "CenterUserBO", value = "用户数据", required = true)
            @RequestBody CenterUserBO userBO,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response){

        if (result.hasErrors()){
            // 判断BindingResult是否保存错误的验证信息，如果有，则直接return
            return JSONResult.errorMap(getErrors(result));
        }

        Users userInfo = centerUserService.updateUserInfo(userId, userBO);

        // TODO 后续需要整合redis通过token实现分布式会话
        // 重置客户端cookie
        setNullProperty(userInfo);
        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(userInfo), true);
        return JSONResult.ok();
    }

    private Map<String, String> getErrors(BindingResult result){
        HashMap<String, String> map = new HashMap<>();

        List<FieldError> errors = result.getFieldErrors();
        for (FieldError error : errors) {
            // 发生验证错误所对应的某一个属性
            String errorField = error.getField();
            // 验证错误的信息
            String errorMsg = error.getDefaultMessage();

            map.put(errorField, errorMsg);
        }

        return map;
    }

    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

    @ApiOperation(value = "用户头像修改", notes = "用户头像修改", httpMethod = "POST")
    @PostMapping("uploadFace")
    public JSONResult uploadFace(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam  String userId,
            @ApiParam(name = "file", value = "用户头像", required = true)
            MultipartFile file,
            HttpServletRequest request,
            HttpServletResponse response){

        // 1. 文件上传服务器

        String newFilename = "";
        // 定义头像保存的路径
        String filePath = fileUpload.getImageUserFaceLocation() + File.separator + userId;
        // 保证文件不为空
        if (file != null){
            String filename = file.getOriginalFilename();
            if (!StringUtils.isEmpty(filename)){
                //  文件重命名
                String[] arr = filename.split("\\.");
                // 获取上传文件后缀
                String suffix =  "." + arr[arr.length - 1];

                if (!suffix.equals(".png") && !suffix.equals(".jpg") && !suffix.equals(".jpeg")){
                    return JSONResult.errorMsg("图片格式不正确");
                }
                // 拼接文件名（覆盖式）
                newFilename = "face-" + userId + suffix;

                File outputFile = new File(filePath, newFilename);
                if (outputFile.getParentFile() != null){
                    outputFile.getParentFile().mkdirs();
                }

                // 上传文件
                try (FileOutputStream fos = new FileOutputStream(outputFile);
                     InputStream fis = file.getInputStream()){
                   IOUtils.copy(fis, fos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            return JSONResult.errorMsg("图片名不能为空");
        }

        // 2. 更新数据库图片路径
        String serverFaceImgUrl = fileUpload.getImageServerUrl() + userId + "/" + newFilename +
                "?t=" + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);
        Users userInfo = centerUserService.updateUserFace(userId, serverFaceImgUrl);

        // TODO 后续需要整合redis通过token实现分布式会话
        // 重置客户端cookie
        setNullProperty(userInfo);
        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(userInfo), true);

        return JSONResult.ok();
    }
}
