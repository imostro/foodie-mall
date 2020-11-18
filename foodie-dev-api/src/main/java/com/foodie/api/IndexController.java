package com.foodie.api;

import com.foodie.pojo.Carousel;
import com.foodie.pojo.Category;
import com.foodie.pojo.JSONResult;
import com.foodie.pojo.vo.CategoryVO;
import com.foodie.pojo.vo.NewItemsVO;
import com.foodie.service.CarouseService;
import com.foodie.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/carousel")
    @ApiOperation(value = "首页轮播图", notes = "首页轮播图", httpMethod = "GET")
    public JSONResult carousel(@RequestParam(required = false, defaultValue = "1") Integer isShow){
        List<Carousel> list = carouseService.queryAll(isShow);
        return JSONResult.ok(list);
    }


    @GetMapping("/cats")
    @ApiOperation(value = "首页商品分类", notes = "首页商品分类", httpMethod = "GET")
    public JSONResult rootCats(){
        List<Category> categories = categoryService.queryAllRootCategory();
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

        List<CategoryVO> vos = categoryService.queryCategoryByParentId(rootCatId);
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

        List<NewItemsVO> vos = categoryService.querySixNewItemLazy(rootCatId);
        return JSONResult.ok(vos);
    }
}
