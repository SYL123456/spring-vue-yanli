package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@TableName("news")
@Data
public class News {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    String desc2;
    private String author;

    Integer memberId;
    String memberName;
    //1 图片 2 视频 3 文字
    Integer catId;
    String catName;
    //类别 1 全部分类 2 绿色要闻 3 校园动态  4 教学科研
    Integer type;
    //类别名称
    String typeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    String defaultImage;
    String defaultVideo;
    Integer readCounts;
    String newsFrom;
    Integer newsStatus;//0 审核中 1 已上线 2 下线
    @TableField(exist = false)
    String newsStatusStr;
    @TableField(exist = false)
    Integer shoucang;
}
