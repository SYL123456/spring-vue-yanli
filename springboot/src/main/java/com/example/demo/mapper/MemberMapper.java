package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.controller.dto.UserAddressDto;
import com.example.demo.entity.Member;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface MemberMapper extends BaseMapper<Member> {
    // 一对多查询
    Page<Member> findPage(Page<Member> page, @Param("nickName") String nickName);

    @Select("select count(id) count, address from member GROUP BY address")
    List<UserAddressDto> countAddress();

    // 查询用户名
    @Select("select password from member where username=#{username}")
    Member selectByName(String username);

    @Update("update member set password = #{newPass} where id = #{userId}")
    int updatePass(Map<String, Object> map);
}
