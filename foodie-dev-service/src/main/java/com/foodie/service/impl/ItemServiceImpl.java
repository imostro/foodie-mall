package com.foodie.service.impl;

import com.foodie.enums.CommentLevel;
import com.foodie.enums.YesOrNo;
import com.foodie.mapper.*;
import com.foodie.pojo.*;
import com.foodie.pojo.vo.CommentLevelCountsVO;
import com.foodie.pojo.vo.ItemCommentVO;
import com.foodie.pojo.vo.SearchItemsVO;
import com.foodie.pojo.vo.ShopcartVO;
import com.foodie.service.ItemService;
import com.foodie.utils.DesensitizationUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Gray
 * @date 2020/10/22 23:49
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemsMapper itemsMapper;

    @Autowired
    private ItemsImgMapper itemsImgMapper;

    @Autowired
    private ItemsParamMapper itemsParamMapper;

    @Autowired
    private ItemsSpecMapper itemsSpecMapper;

    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;


    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Items queryItemsByItemId(String itemId) {
        Example example = new Example(Items.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", itemId);
        return itemsMapper.selectOneByExample(example);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<ItemsImg> queryItemImgByItemId(String itemId) {
        Example example = new Example(ItemsImg.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId", itemId);
        return itemsImgMapper.selectByExample(example);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<ItemsSpec> queryItemSpecByItemId(String itemId) {
        Example example = new Example(ItemsSpec.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId", itemId);
        return itemsSpecMapper.selectByExample(example);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public ItemsParam queryItemParamByItemId(String itemId) {
        Example example = new Example(ItemsParam.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId", itemId);
        return itemsParamMapper.selectOneByExample(example);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public CommentLevelCountsVO queryCommentCount(String itemId) {
        Integer goodCounts = getCommentCounts(itemId, CommentLevel.GOOD.type);
        Integer normalCounts = getCommentCounts(itemId, CommentLevel.NORMAL.type);
        Integer badCounts = getCommentCounts(itemId, CommentLevel.BAD.type);
        Integer totalCounts = goodCounts + normalCounts + badCounts;

        CommentLevelCountsVO countsVO = new CommentLevelCountsVO();
        countsVO.setGoodCounts(goodCounts);
        countsVO.setNormalCounts(normalCounts);
        countsVO.setBadCounts(badCounts);
        countsVO.setTotalCounts(totalCounts);
        return countsVO;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public PagedGridResult queryPagedComments(String itemId,
                                              Integer level,
                                              Integer page,
                                              Integer size) {
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("itemId", itemId);
        map.put("level", level);

        PageHelper.startPage(page, size);

        List<ItemCommentVO> list = itemsMapperCustom.queryItemComments(map);
        for (ItemCommentVO vo : list) {
            vo.setNickname(DesensitizationUtil.commonDisplay(vo.getNickname()));
        }

        return setterPagedGird(list, page);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public PagedGridResult searchItems(String keywords, String sort, Integer page, Integer size) {
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("keywords", keywords);
        map.put("sort", sort);

        PageHelper.startPage(page, size);
        List<SearchItemsVO> list = itemsMapperCustom.searchItems(map);

        return setterPagedGird(list, page);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public PagedGridResult searchItems(Integer catId, String sort, Integer page, Integer size) {
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("catId", catId);
        map.put("sort", sort);

        PageHelper.startPage(page, size);
        List<SearchItemsVO> list = itemsMapperCustom.searchItemsByThirdCat(map);
        return setterPagedGird(list, page);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<ShopcartVO> queryItemsBySpecIds(String itemSpecIds) {
        String[] ids = itemSpecIds.split(",");
        List<String> idList = Arrays.asList(ids);

        return itemsMapperCustom.queryItemsBySpecIds(idList);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public ItemsSpec queryItemsSpecBySpecId(String specId) {
        return itemsSpecMapper.selectByPrimaryKey(specId);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<ItemsSpec> queryItemsSpecListBySpecIds(List<String> specIds) {
        Example example = new Example(ItemsSpec.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", specIds);

        return itemsSpecMapper.selectByExample(example);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public String queryMainItemImgByItemsId(String itemsId) {
        ItemsImg itemsImg = new ItemsImg();
        itemsImg.setItemId(itemsId);
        itemsImg.setIsMain(YesOrNo.YES.type);
        List<ItemsImg> mainImgs = itemsImgMapper.select(itemsImg);
        return mainImgs != null && mainImgs.size() >0 ? mainImgs.get(0).getUrl() : "";
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void decreaseSpecStock(String specId, int buyCounts) {
        // synchronized 不推荐，集群下无用，性能低下
        // 锁数据库： 不推荐，导致数据库性能低下
        // 分布式锁 zookeeper redis

        // lockUtil.getLock(); -- 加锁
        // lockUtil.unlock(); -- 解锁

        int result = itemsMapperCustom.decreaseItemSpecStock(specId, buyCounts);// 通过SQL采用乐观锁来解决超卖
        if (result != 1){
            throw new RuntimeException("订单创建失败，原因：库存不足！");
        }
    }


    Integer getCommentCounts(String itemId, Integer level){
        ItemsComments comments = new ItemsComments();
        comments.setItemId(itemId);
        if (level != null){
            comments.setCommentLevel(level);
        }
        return itemsCommentsMapper.selectCount(comments);
    }
}
