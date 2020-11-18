package com.foodie.service;

import com.foodie.pojo.*;
import com.foodie.pojo.vo.CommentLevelCountsVO;
import com.foodie.pojo.vo.ShopcartVO;

import java.util.List;

/**
 * @author Gray
 * @date 2020/10/22 23:49
 */
public interface ItemService extends BaseService {

    /**
     * 根据商品id查询商品数据
     * @param itemId item id
     * @return item data
     */
    Items queryItemsByItemId(String itemId);

    /**
     * 根据商品id查询商品图片
     * @param itemId item id
     * @return item img
     */
    List<ItemsImg> queryItemImgByItemId(String itemId);

    /**
     * 根据商品id查询商品详情
     *
     * @param itemId item id
     * @return item spec
     */
    List<ItemsSpec> queryItemSpecByItemId(String itemId);

    /**
     * 根据商品id查询商品参数
     * @param itemId item id
     * @return item param
     */
    ItemsParam queryItemParamByItemId(String itemId);

    /**
     * 根据商品id查询商品参数
     *
     * @param itemId 商品id
     * @return Item CommentLevelCountsVO
     */
    CommentLevelCountsVO queryCommentCount(String itemId);

    /**
     * 根据商品id进行分页查询
     * @param itemId item id
     * @param level 评论等级
     * @param page 当前页
     * @param size 每页数量
     * @return
     */
    PagedGridResult queryPagedComments(String itemId,
                                       Integer level,
                                       Integer page, Integer size);

    /**
     * 根据keywords搜索商品
     * @param keywords 搜索关键字
     * @param sort 排序字段
     * @param page 当前页
     * @param size 每页数据量
     * @return
     */
    PagedGridResult searchItems(String keywords,
                                String sort,
                                Integer page,
                                Integer size);

    /**
     * 根据分类id搜索商品
     * @param catId 分类id
     * @param sort 排序字段
     * @param page 当前页
     * @param size 每页数据量
     * @return
     */
    PagedGridResult searchItems(Integer catId,
                                String sort,
                                Integer page,
                                Integer size);

    /**
     * 根据商品规格id刷新商品数据
     * @param itemSpecIds 商品规格Id
     * @return  购物车商品数据
     */
    List<ShopcartVO> queryItemsBySpecIds(String itemSpecIds);

    /**
     * 根据商品规格id查询商品规格
     * @param specId 商品规格id
     * @return 商品规格
     */
    ItemsSpec queryItemsSpecBySpecId(String specId);

    /**
     * 根据商品规格Id列表查询规格数据
     * @param specIds 商品规格id列表
     * @return 商品规格列表
     */
    List<ItemsSpec> queryItemsSpecListBySpecIds(List<String> specIds);


    /**
     * 根据商品Id查询主商品图片路径
     * @param itemsId 商品id
     * @return 图片路径
     */
    String queryMainItemImgByItemsId(String itemsId);

    /**
     * 扣除库存
     * @param specId 商品规格id
     * @param buyCounts 购买数
     */
    void decreaseSpecStock(String specId, int buyCounts);
}
