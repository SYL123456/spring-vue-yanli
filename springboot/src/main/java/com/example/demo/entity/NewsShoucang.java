package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("news_shoucang")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsShoucang {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer newsId;
    @TableField(exist = false)
    String newsTitle;
    private Integer memberId;
    private Date created;
}
