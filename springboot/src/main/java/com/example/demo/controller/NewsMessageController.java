package com.example.demo.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.client.PythonClient;
import com.example.demo.common.Result;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/newsMessage")
public class NewsMessageController extends BaseController {
    @Resource
    private NewsMessageMapper newsMessageMapper;
    @Resource
    private NewsMapper newsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private MemberMapper memberMapper;
    @Resource
    HttpServletRequest request;
    @Resource
    private PythonClient pythonClient;

    @PostMapping("/updateStatusDown")
    public Result<?> updateStatusDown(@RequestBody NewsMessage newsMessage) {
        newsMessageMapper.updateStatus(0, newsMessage.getId());
        return Result.success();
    }

    @PostMapping("/updateStatusUp")
    public Result<?> updateStatusUp(@RequestBody NewsMessage newsMessage) {
        newsMessageMapper.updateStatus(1, newsMessage.getId());
        return Result.success();
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody NewsMessage Message) {
        Message.setTime(DateUtil.formatDateTime(new Date()));
        Message.setMessageStatus(0);
        return Result.success(newsMessageMapper.insert(Message));
    }

    @PutMapping
    public Result<?> update(@RequestBody NewsMessage Message) {
        return Result.success(newsMessageMapper.updateById(Message));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        newsMessageMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Long id) {
        return Result.success(newsMessageMapper.selectById(id));
    }

    @GetMapping
    public Result<?> findAll() {
        return Result.success(newsMessageMapper.selectList(null));
    }

    @GetMapping("/foreign/{id}")
    public Result<?> foreign(@PathVariable Integer id) {
        return Result.success(findByForeign(id));
    }

    @GetMapping("/test2/{title}")
    public Result<?> test2(@PathVariable String title) {
        String zmdf = initZmdf(title);
        String fmdf = getFmdf(zmdf);
        Map rst = new HashMap();
        rst.put("zmdf", zmdf);
        rst.put("fmdf", fmdf);

        return Result.success(rst);
    }

    @GetMapping("/test/{id}")
    public Result<?> test(@PathVariable Integer id) {
        NewsMessage newsMessage = newsMessageMapper.selectById(id);
        String zmdf = initZmdf(newsMessage.getContent());
        String fmdf = getFmdf(zmdf);
        Map rst = new HashMap();
        rst.put("zmdf", zmdf);
        rst.put("fmdf", fmdf);

        return Result.success(rst);
    }

    /**
     * ?????????????????????http://localhost:9876/api/newsMessage/page?pageNum=1&pageSize=10&queryMstatus=0&search=
     * ?????????????????????http://localhost:9876/api/newsMessage/page?pageNum=1&pageSize=5&search=&newsId=12
     */
    @GetMapping("/page")
    public Result<?> findNewPage(@RequestParam(required = false, defaultValue = "1") Integer queryMstatus,
                                 @RequestParam(required = false, defaultValue = "") String name,
                                 @RequestParam(required = false, defaultValue = "") Integer newsId,
                                 @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                 @RequestParam(required = false, defaultValue = "10") Integer pageSize) throws InterruptedException {

        IPage<NewsMessage> pages = getNewsMessage(queryMstatus, name, newsId, pageNum, pageSize);
        List<NewsMessage> newsMessage = pages.getRecords();

        BigDecimal t_zm = BigDecimal.ZERO;
        BigDecimal t_fm = BigDecimal.ZERO;

        Map rst = new HashMap<>();

        // ??????????????????
        if (CollectionUtil.isEmpty(newsMessage)) {
            rst.put("page", pages);

            //??????
            if (t_zm.compareTo(t_fm) == 1) {
                rst.put("zm", 1);
            } else {
                rst.put("zm", 0);
            }
            return Result.success(rst);
        }

        // ?????????????????????
        List<Long> newMessageNewsId = newsMessage.stream().map(NewsMessage::getNewsId).distinct().collect(Collectors.toList());
        List<News> newsList = newsMapper.selectList(Wrappers.<News>lambdaQuery().in(News::getId, newMessageNewsId));
        Map<Integer, String> newsIdAndTitleMap = newsList.stream().collect(Collectors.toMap(News::getId, News::getTitle));

        List<String> memberNameList = newsMessage.stream().map(NewsMessage::getUsername).distinct().collect(Collectors.toList());
        List<Member> memberList = memberMapper.selectList(Wrappers.<Member>lambdaQuery().in(Member::getUsername, memberNameList));
        Map<String, String> memberUserNameAndAvatarMap = memberList.stream().collect(Collectors.toMap(Member::getUsername, Member::getAvatar));

        for (NewsMessage message : newsMessage) {
            // ??????????????????
            int newId = message.getNewsId().intValue();
            message.setTitle(newsIdAndTitleMap.getOrDefault(newId, "??????????????????"));
            // ??????????????????
            String username = message.getUsername();
            message.setAvatar(memberUserNameAndAvatarMap.getOrDefault(username, "http://localhost:9090/files/preview/1.png"));

            // ????????????????????????,?????????newsId????????????????????????????????????
            if (!Objects.isNull(newsId)) {
//                String zmdf = initZmdfTest(message.getContent());
                String zmdf = initZmdf(message.getContent());
                String fmdf = getFmdf(zmdf);
                message.setZmdf(zmdf);
                message.setFmdf(fmdf);

                t_zm = getTotal(t_zm, zmdf);
                t_fm = getTotal(t_fm, fmdf);
            }

            // ?????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????id????????????????????????parentId?????????????????????????????????????????????????????????Message::setParentMessage
            Long parentId = message.getParentId();
            newsMessage.stream().filter(c -> c.getId().equals(parentId)).findFirst().ifPresent(message::setParentMessage);
        }

        rst.put("fmdf", t_fm.divide(BigDecimal.valueOf(newsMessage.size()), 2, BigDecimal.ROUND_HALF_UP));
        rst.put("zmdf", t_zm.divide(BigDecimal.valueOf(newsMessage.size()), 2, BigDecimal.ROUND_HALF_UP));


        pages.setRecords(newsMessage);
        rst.put("page", pages);

        // ??????
        if (t_zm.compareTo(t_fm) == 1) {
            rst.put("zm", 1);
        } else {
            rst.put("zm", 0);
        }
        return Result.success(rst);
    }


    /**
     * ??????????????????
     */
    private IPage<NewsMessage> getNewsMessage(Integer queryMstatus,
                                              String name,
                                              Integer newsId,
                                              Integer pageNum,
                                              Integer pageSize) {
        LambdaQueryWrapper<NewsMessage> query = Wrappers.<NewsMessage>lambdaQuery()
                .like(NewsMessage::getContent, name).
                orderByDesc(NewsMessage::getId);

        if (newsId != null) {
            query.eq(NewsMessage::getNewsId, newsId);
        }

        if (queryMstatus != null && queryMstatus != -1) {
            query.eq(NewsMessage::getMessageStatus, queryMstatus);
        }

        return newsMessageMapper.selectPage(new Page<>(pageNum, pageSize), query);
    }

    String initZmdf(String str) {
        if (str == null || str.length() == 0) {
            return "0";
        }
        Long startTime = System.currentTimeMillis();

        AttitudeResp pythonResult = pythonClient.getPythonResult(str);
        if (Objects.isNull(pythonResult)) {
            System.out.println("??????python????????????????????????");
            return "0";
        }
        // 0.9234234
        String score = pythonResult.getScore();
        BigDecimal result = new BigDecimal(score);

        Long endTime = System.currentTimeMillis();

        System.out.println("??????: " + (endTime - startTime) + "??????");
        System.out.println("??????: " + (endTime - startTime) / 1000 + "???");

        return result.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    String initZmdfTest(String str) throws InterruptedException {
        if (str == null || str.length() == 0) {
            return "0";
        }
        BigDecimal bigDecimal = new BigDecimal(0.99923242);
        Thread.sleep(1000);
        return bigDecimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_UP).toString();
    }


    String getFmdf(String zmdf) {
        BigDecimal z = new BigDecimal(zmdf).setScale(6, BigDecimal.ROUND_HALF_UP);
        BigDecimal f = BigDecimal.valueOf(100).subtract(z);
        return f.toString();
    }

    BigDecimal getTotal(BigDecimal start, String df) {
        BigDecimal z = new BigDecimal(df).setScale(6, BigDecimal.ROUND_HALF_UP);
        return start.add(z);
    }

    @GetMapping("/myMessage")
    public Result<?> myMessage(@RequestParam(required = false, defaultValue = "") String memberName,
                               @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                               @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<NewsMessage> query = Wrappers.<NewsMessage>lambdaQuery().like(NewsMessage::getUsername, memberName).orderByDesc(NewsMessage::getId);

        IPage<NewsMessage> pages = newsMessageMapper.selectPage(new Page<>(pageNum, pageSize), query);

        return Result.success(pages);
    }

    private List<NewsMessage> findByForeign(Integer foreignId) {
        // ?????? foreignId 0 ???????????????????????????
        LambdaQueryWrapper<NewsMessage> queryWrapper =
                Wrappers.<NewsMessage>lambdaQuery().eq(NewsMessage::getNewsId, foreignId).orderByDesc(NewsMessage::getId);
        List<NewsMessage> list = newsMessageMapper.selectList(queryWrapper);
        // ????????????????????????
        for (NewsMessage Message : list) {
            Member one = memberMapper.selectOne(Wrappers.<Member>lambdaQuery().eq(Member::getUsername, Message.getUsername()));
            if (StrUtil.isNotBlank(one.getAvatar())) {
                Message.setAvatar(one.getAvatar());
            } else {
                // ??????????????????
                Message.setAvatar("http://localhost:9090/files/preview/1.png");
            }
            Long parentId = Message.getParentId();
            // ?????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????id????????????????????????parentId?????????????????????????????????????????????????????????Message::setParentMessage
            list.stream().filter(c -> c.getId().equals(parentId)).findFirst().ifPresent(Message::setParentMessage);
        }
        return list;
    }


}
