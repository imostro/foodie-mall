package com.foodie.api;

import com.foodie.pojo.Carousel;
import com.foodie.pojo.Category;
import com.foodie.pojo.JSONResult;
import com.foodie.pojo.vo.CategoryVO;
import com.foodie.pojo.vo.NewItemsVO;
import com.foodie.service.CarouseService;
import com.foodie.service.CategoryService;
import com.foodie.utils.JsonUtils;
import com.foodie.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Gray
 * @date 2020/10/22 18:04
 */
@Api(value = "首页", tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private CarouseService carouseService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisOperator redisOperator;

    @GetMapping("/carousel")
    @ApiOperation(value = "首页轮播图", notes = "首页轮播图", httpMethod = "GET")
    public JSONResult carousel(@RequestParam(required = false, defaultValue = "1") Integer isShow){
        List<Carousel> list;

        String carouselStr = redisOperator.get("carousel");
        if(StringUtils.isEmpty(carouselStr)){
            list = carouseService.queryAll(isShow);
            redisOperator.set("carousel", JsonUtils.objectToJson(list));
        }else{
            list = JsonUtils.jsonToList(carouselStr, Carousel.class);
        }

        /*
         *  如何重置缓存？
         * 1. 后台运营系统，一旦广告（轮播图）发生更改，就可以删除缓存，然后重置
         * 2. 定时重置，比如每天凌晨三点重置
         * 3. 每个轮播图都有可能是一个广告，每个广告都会有一个过期时间，过期了，再重置
         */

        return JSONResult.ok(list);
    }


    @GetMapping("/cats")
    @ApiOperation(value = "首页商品分类", notes = "首页商品分类", httpMethod = "GET")
    public JSONResult rootCats(){

        List<Category> categories;
        String catsStr = redisOperator.get("cats");
        if (StringUtils.isEmpty(catsStr)){
            categories = categoryService.queryAllRootCategory();
            redisOperator.set("cats", JsonUtils.objectToJson(categories));
        }else{
            categories = JsonUtils.jsonToList(catsStr, Category.class);
        }

        return JSONResult.ok(categories);
    }

    @GetMapping("/subCat/{rootCatId}")
    @ApiOperation(value = "首页商品子分类", notes = "首页商品子分类", httpMethod = "GET")
    public JSONResult subCats(@ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId){
        // 防止恶意攻击
        if (rootCatId == null){
            return JSONResult.ok("Id不能为空");
        }
        // 防止缓存穿透
        if (rootCatId < 0){
            return JSONResult.errorMsg("Id不存在");
        }

        List<CategoryVO> vos;
        String catsStr = redisOperator.get("subCat:" + rootCatId);
        if (StringUtils.isEmpty(catsStr)){
            vos = categoryService.queryCategoryByParentId(rootCatId);
            // 即使为空，也放入到redis中，可以防止缓存穿透
            redisOperator.set("subCat:" + rootCatId, JsonUtils.objectToJson(vos));
        }else{
            vos = JsonUtils.jsonToList(catsStr, CategoryVO.class);
        }

        return JSONResult.ok(vos);
    }

    @GetMapping("/sixNewItems/{rootCatId}")
    @ApiOperation(value = "商品推荐", notes = "商品推荐", httpMethod = "GET")
    public JSONResult sixNewItems(@ApiParam(name = "rootCatId", value = "一级分类id", required = true)
                              @PathVariable Integer rootCatId){
        // 防止恶意攻击
        if (rootCatId == null){
            return JSONResult.ok("Id不能为空");
        }
        // 防止缓存穿透
        if (rootCatId < 0){
            return JSONResult.errorMsg("Id不存在");
        }

        List<NewItemsVO> vos = categoryService.querySixNewItemLazy(rootCatId);
        return JSONResult.ok(vos);
    }
}
