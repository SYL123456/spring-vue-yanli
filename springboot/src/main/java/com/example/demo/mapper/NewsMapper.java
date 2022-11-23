package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Book;
import com.example.demo.entity.News;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

public interface NewsMapper extends BaseMapper<News> {


    @Update("update news set news_status = #{new_status} where id = #{id}")
    int updateStatus(int new_status,int id);
}
