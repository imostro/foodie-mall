package com.foodie.mapper;

import com.foodie.my.mapper.MyMapper;
import com.foodie.pojo.ItemsComments;
import com.foodie.pojo.vo.ItemCommentVO;
import com.foodie.pojo.vo.MyCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsCommentsMapperCustom extends MyMapper<ItemsComments> {

    public void saveComments(Map<String, Object> map);

    public List<MyCommentVO> queryMyComments(@Param("paramsMap") Map<String, Object> map);

}