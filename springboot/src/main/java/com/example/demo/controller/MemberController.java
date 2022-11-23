package com.example.demo.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.*;
import com.example.demo.enums.PwdEnum;
import com.example.demo.enums.RoleEnum;
import com.example.demo.mapper.MemberMapper;
import com.example.demo.mapper.PermissionMapper;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.mapper.UserRoleMapper;
import com.example.demo.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/member")
public class MemberController extends BaseController {

    @Resource
    MemberMapper memberMapper;
    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder; //注入bcryct加密

    @PostMapping("/login")
    public Result<?> login(@RequestBody Member userParam, HttpServletRequest request) {
        Member userPwd = memberMapper.selectByName(userParam.getUsername());
        if(userPwd == null){
            return Result.error("-1", "用户名错误");
        }
        QueryWrapper<Member> queryWrapper = new QueryWrapper<Member>();
        queryWrapper.eq("username", userParam.getUsername());
        queryWrapper.eq("password", userPwd.getPassword());
        Member res = memberMapper.selectOne(queryWrapper);

        // 判断密码是否正确
        if (!bCryptPasswordEncoder.matches(userParam.getPassword(), userPwd.getPassword())) {
            return Result.error("-1", "密码错误");
        }
        if (res == null) {
            return Result.error("-1", "用户名或密码错误");
        }

        request.getSession().setAttribute(userParam.getUsername(),true);
        return Result.success(res);
    }
    @PostMapping("/loginout")
    public Result<?> loginout(@RequestBody Member userParam, HttpServletRequest request) {
        request.getSession().removeAttribute(userParam.getUsername());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody Member member) {
        Member res = memberMapper.selectOne(Wrappers.<Member>lambdaQuery().eq(Member::getUsername, member.getUsername()));
        if (res != null) {
            return Result.error("-1", "用户名重复");
        }
        Member userInfo = Member.builder()
                .username(member.getUsername())
                .password(bCryptPasswordEncoder.encode(member.getPassword()))
                .nickName("用户" + IdWorker.getId())
                .build();

        memberMapper.insert(userInfo);

        return Result.success();
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody Member user) {
//        if (user.getPassword() == null) {
//            user.setPassword(bCryptPasswordEncoder.encode(PwdEnum.PASSWORD.getPassword()));
//        }
        memberMapper.insert(user);
//
//        UserRole userRole = UserRole.builder()
//                .userId(user.getId())
//                .roleId(RoleEnum.USER.getRoleId())
//                .build();
//        userRoleMapper.insert(userRole);

        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> update(@RequestBody Member user) {
        memberMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/pass")
    public Result<?> pass(@RequestBody Map<String, Object> map) {
        Member user = memberMapper.selectById((Integer) map.get("userId"));
        if (user== null) {
            return Result.error("-1", "未找到用户");
        }
        if (!bCryptPasswordEncoder.matches(map.get("password").toString(), user.getPassword())) {
            return Result.error("-1", "密码错误");
        }
        map.put("newPass", (bCryptPasswordEncoder.encode(map.get("newPass").toString())));
        memberMapper.updatePass(map);
        return Result.success();
    }


    @DeleteMapping("/{id}")
    public Result<?> update(@PathVariable Long id) {
        memberMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(memberMapper.selectById(id));
    }

    @GetMapping("/all")
    public Result<?> findAll() {
        return Result.success(memberMapper.selectList(null));
    }

    /**
     * 统计数据
     *
     * @return
     */
    @GetMapping("/count")
    public Result<?> count() {
//        User user = getUser(); // 当前登录的用户信息
        return Result.success(memberMapper.countAddress());
    }

    /**
     * 用户分页列表查询，包含书籍表的一对多查询
     *
     * @param pageNum
     * @param pageSize
     * @param search
     * @return
     */
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery().orderByAsc(User::getId);
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(User::getNickName, search);
        }

        Page<Member> userPage = memberMapper.findPage(new Page<>(pageNum, pageSize), search);

        return Result.success(userPage);
    }

//    /**
//     * Excel导出
//     *
//     * @param response
//     * @throws IOException
//     */
//    @GetMapping("/export")
//    public void export(HttpServletResponse response) throws IOException {
//
//        List<Map<String, Object>> list = CollUtil.newArrayList();
//
//        List<User> all = userMapper.selectList(null);
//        for (User user : all) {
//            Map<String, Object> row1 = new LinkedHashMap<>();
//            row1.put("用户名", user.getUsername());
//            row1.put("昵称", user.getNickName());
//            row1.put("年龄", user.getAge());
//            row1.put("性别", user.getSex());
//            row1.put("地址", user.getAddress());
//            list.add(row1);
//        }
//
//        // 2. 写excel
//        ExcelWriter writer = ExcelUtil.getWriter(true);
//        writer.write(list, true);
//
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
//        String fileName = URLEncoder.encode("用户信息", "UTF-8");
//        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
//
//        ServletOutputStream out = response.getOutputStream();
//        writer.flush(out, true);
//        writer.close();
//        IoUtil.close(System.out);
//    }
//
//    /**
//     * Excel导入
//     * 导入的模板可以使用 Excel导出的文件
//     *
//     * @param file Excel
//     * @return
//     * @throws IOException
//     */
//    @PostMapping("/import")
//    public Result<?> upload(MultipartFile file) throws IOException {
//        InputStream inputStream = file.getInputStream();
//        List<List<Object>> lists = ExcelUtil.getReader(inputStream).read(1);
//        List<User> saveList = new ArrayList<>();
//        for (List<Object> row : lists) {
//            User user = new User();
//            user.setUsername(row.get(0).toString());
//            user.setNickName(row.get(1).toString());
//            user.setAge(Integer.valueOf(row.get(2).toString()));
//            user.setSex(row.get(3).toString());
//            user.setAddress(row.get(4).toString());
//            saveList.add(user);
//        }
//        for (User user : saveList) {
//            userMapper.insert(user);
//        }
//        return Result.success();
//    }

}
