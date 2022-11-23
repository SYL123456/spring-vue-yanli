package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;


@Data
@TableName("news_message")
public class NewsMessage extends Model<NewsMessage> {
    /**
      * 主键
      */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
      * 内容
      */
    private String content;

    /**
      * 评论人
      */
    private String username;

    /**
      * 评论时间
      */
    private String time;

    /**
      * 父ID
      */
    private Long parentId;

    Integer messageStatus;//0 待审核 1审核通过

    @TableField(exist = false)
    private NewsMessage parentMessage;

    /**
     * 新闻id
     */
    private Long newsId;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String zmdf;
    @TableField(exist = false)
    private String fmdf;
    @TableField(exist = false)
    private String title;


}
