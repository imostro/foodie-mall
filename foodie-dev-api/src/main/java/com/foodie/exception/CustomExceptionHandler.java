package com.foodie.exception;

import com.foodie.pojo.JSONResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @Author: Gray
 * @date 2020/11/3 23:35
 */
@RestControllerAdvice
public class CustomExceptionHandler {

    // 破获MaxUploadSizeExceededException
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public JSONResult handlerMaxUploadFile(MaxUploadSizeExceededException ex){
        return JSONResult.errorMsg("上传文件超过500KB,请压缩或降图片质量");
    }
}
