package com.foodie.api;

import com.foodie.pojo.*;
import com.foodie.pojo.vo.CommentLevelCountsVO;
import com.foodie.pojo.vo.ItemInfoVO;
import com.foodie.pojo.vo.ShopcartVO;
import com.foodie.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Gray
 * @date 2020/10/22 23:59
 */
@Api(value = "商品", tags = {"商品详情的相关接口"})
@RestController
@RequestMapping("items")
public class ItemController {

    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;


    @Autowired
    private ItemService itemService;

    @ApiOperation(value = "商品详细信息", notes = "商品详细信息", httpMethod = "GET")
    @GetMapping("info/{itemId}")
    public JSONResult info(@ApiParam(name = "itemId", value = "商品id", required = true)
            @PathVariable String itemId){
        if (itemId == null){
            return JSONResult.errorMsg("");
        }

        Items items = itemService.queryItemsByItemId(itemId);
        List<ItemsImg> imgList = itemService.queryItemImgByItemId(itemId);
        ItemsParam itemsParam = itemService.queryItemParamByItemId(itemId);
        List<ItemsSpec> itemsSpecList = itemService.queryItemSpecByItemId(itemId);

        ItemInfoVO infoVO = new ItemInfoVO();
        infoVO.setItem(items);
        infoVO.setItemImgList(imgList);
        infoVO.setItemParams(itemsParam);
        infoVO.setItemSpecList(itemsSpecList);

        return JSONResult.ok(infoVO);
    }

    @ApiOperation(value = "查询商品评价总数", notes = "查询商品评价总数", httpMethod = "GET")
    @GetMapping("/commentLevel")
    public JSONResult commentLevel(@ApiParam(name = "itemId", value = "商品id", required = true)
                           @RequestParam String itemId){
        if (itemId == null){
            return JSONResult.errorMsg("");
        }
        CommentLevelCountsVO vo = itemService.queryCommentCount(itemId);
        return JSONResult.ok(vo);
    }

    @ApiOperation(value = "查询商品评论", notes = "查询商品评论", httpMethod = "GET")
    @GetMapping("/comments")
    public JSONResult comments(
            @ApiParam(name = "itemId", value = "商品id", required = true)
            @RequestParam String itemId,
            @ApiParam(name = "level", value = "评价等级")
            @RequestParam(required = false) Integer level,
            @ApiParam(defaultValue = "1", name = "page", value = "查询下一页的第几页")
            @RequestParam(defaultValue = "1",required = false) Integer page,
            @ApiParam(defaultValue = "5",name = "pageSize", value = "分页的每一页显示的条数")
            @RequestParam(defaultValue = "5",required = false) Integer pageSize) {

        if (StringUtils.isEmpty(itemId)){
            return JSONResult.errorMsg(null);
        }

        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult grid = itemService.queryPagedComments(itemId,
                level,
                page,
                pageSize);

        return JSONResult.ok(grid);
    }


    @ApiOperation(value = "搜索商品数据", notes = "搜索商品数据", httpMethod = "GET")
    @GetMapping("/search")
    public JSONResult searchItem(
            @ApiParam(name = "keywords", value = "搜索关键字", required = true)
            @RequestParam String keywords,
            @ApiParam(name = "sort", value = "排序")
            @RequestParam(defaultValue = "k",required = false) String sort,
            @ApiParam(name = "page", value = "查询下一页的第几页")
            @RequestParam(required = false) Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数")
            @RequestParam(required = false) Integer pageSize){

        if (StringUtils.isEmpty(keywords)){
            return JSONResult.errorMsg(null);
        }
        if (page == null || page<=0){
            page = 1;
        }

        if (pageSize == null){
            pageSize = PAGE_SIZE;
        }
        PagedGridResult items = itemService.searchItems(keywords, sort, page, pageSize);
        return JSONResult.ok(items);
    }

    @ApiOperation(value = "搜索商品数据", notes = "搜索商品数据", httpMethod = "GET")
    @GetMapping("/catItems")
    public JSONResult catItems(
            @ApiParam(name = "keywords", value = "搜索关键字", required = true)
            @RequestParam Integer catId,
            @ApiParam(name = "sort", value = "排序")
            @RequestParam(defaultValue = "k",required = false) String sort,
            @ApiParam(name = "page", value = "查询下一页的第几页")
            @RequestParam(required = false) Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数")
            @RequestParam(required = false) Integer pageSize){

        if (StringUtils.isEmpty(catId)){
            return JSONResult.errorMsg(null);
        }
        if (page == null || page<=0){
            page = 1;
        }

        if (pageSize == null){
            pageSize = PAGE_SIZE;
        }
        PagedGridResult items = itemService.searchItems(catId, sort, page, pageSize);
        return JSONResult.ok(items);
    }

    @ApiOperation(value = "刷新购物车商品数据", notes = "刷新购物车商品数据", httpMethod = "GET")
    @GetMapping("/refresh")
    public JSONResult refresh(
            @ApiParam(name = "keywords", value = "搜索关键字", required = true, example = "10,12,13")
            @RequestParam String itemSpecIds){

        if (StringUtils.isEmpty(itemSpecIds)){
            return JSONResult.ok();
        }

        List<ShopcartVO> list = itemService.queryItemsBySpecIds(itemSpecIds);

        return JSONResult.ok(list);
    }
}
