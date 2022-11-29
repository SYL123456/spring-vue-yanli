package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.NewsMessage;
import org.apache.ibatis.annotations.Update;

public interface NewsMessageMapper extends BaseMapper<NewsMessage> {

    @Update("update news_message set message_status = #{message_status} where id = #{id}")
    int updateStatus(int message_status,long id);


    @Update("update news_message set sort = 1  where id = #{id}")
    int updateSort(long id);
    @Update("update news_message set sort = 0  where id = #{id}")
    int updateSort2(long id);
}
