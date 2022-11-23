package com.example.demo.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Constants;
import com.example.demo.common.Result;
import com.example.demo.entity.Member;
import com.example.demo.entity.News;
import com.example.demo.entity.NewsShoucang;
import com.example.demo.enums.NewsCatEnum;
import com.example.demo.enums.NewsTypeEnum;
import com.example.demo.mapper.MemberMapper;
import com.example.demo.mapper.NewsMapper;
import com.example.demo.mapper.NewsMessageMapper;
import com.example.demo.mapper.NewsShoucangMapper;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/news")
public class NewsController extends BaseController {

    @Resource
    NewsMapper newsMapper;
    @Resource
    NewsShoucangMapper newsShoucangMapper;
    @Resource
    MemberMapper memberMapper;
    @Resource
    NewsMessageMapper newsMessageMapper;

    @GetMapping("/listNewsPic")
    public Result<?> listNewsPic() {
        LambdaQueryWrapper<News> wrapper = Wrappers.<News>lambdaQuery();

        wrapper.orderByDesc(News::getReadCounts);
        Page<News> newsPage = newsMapper.selectPage(new Page<>(1, 5), wrapper);
        List<News> news = newsPage.getRecords();
        List<PicRes> rst= new ArrayList();
        for(int i =0;i<news.size();i++){
            PicRes p = new PicRes();
            if(StringUtils.isNotBlank(news.get(i).getDefaultImage())){
                p.img = news.get(i).getDefaultImage().replace("localhost", Constants.DEFAULT_IP);
            }
            p.id = news.get(i).getId();
            rst.add(p);
        }

        return Result.success(rst);
    }
    @Data
    class PicRes{
        String img;
        int id;
    }
    @GetMapping("/summary2")
    public Result<?> summary2() {
        LambdaQueryWrapper<News> wrapper = Wrappers.<News>lambdaQuery();

        wrapper.orderByDesc(News::getReadCounts);
        Page<News> newsPage = newsMapper.selectPage(new Page<>(1, 5), wrapper);
        List<News> news = newsPage.getRecords();
        List news_arr = new ArrayList();
        List count_arr = new ArrayList();
        for(int i =0;i<news.size();i++){
            if(news.get(i).getTitle().length()>5){
                news_arr.add(news.get(i).getTitle().substring(0,5));
            }
            else{
                news_arr.add(news.get(i).getTitle());
            }
            count_arr.add(news.get(i).getReadCounts());
        }

        Map rst = new HashMap<>();
        rst.put("news_arr",news_arr);
        rst.put("count_arr",count_arr);

        return Result.success(rst);
    }
    @GetMapping("/summary")
    public Result<?> summary() {
        //访问量

        //用户量
        int members = memberMapper.selectCount(null);
        //新闻量
        int newsc = newsMapper.selectCount(null);
        //评论量
        int newsMessage = newsMessageMapper.selectCount(null);

        Map rst = new HashMap<>();
        rst.put("members",members);
        rst.put("newsc",newsc);
        rst.put("newsMessage",newsMessage);

        return Result.success(rst);
    }

    @GetMapping("/findShoucangPage")
    public Result<?> findShoucangPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam() Integer memberId) {
        LambdaQueryWrapper<NewsShoucang> wrapper = Wrappers.<NewsShoucang>lambdaQuery();
        wrapper.eq(NewsShoucang::getMemberId,memberId);
        wrapper.orderByDesc(NewsShoucang::getId);
        Page<NewsShoucang> newsPage = newsShoucangMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<NewsShoucang> news = newsPage.getRecords();
        news.stream().forEach(obj->obj.setNewsTitle(getNewsTitle(obj.getNewsId())));
        return Result.success(newsPage);
    }

    String getNewsTitle(int id){
        News n = newsMapper.selectById(id);
        if(n!=null){
            return n.getTitle();
        }else{
            return "";
        }
    }


    @PostMapping("/deleteShoucang")
    public Result<?> deleteShoucang(@RequestBody NewsShoucang newssc) {
        LambdaQueryWrapper<NewsShoucang> wrapper = Wrappers.<NewsShoucang>lambdaQuery();
        wrapper.eq(NewsShoucang::getMemberId,newssc.getMemberId());
        wrapper.eq(NewsShoucang::getNewsId,newssc.getNewsId());
        newsShoucangMapper.delete(wrapper);
        return Result.success();
    }
    @PostMapping("/saveShoucang")
    public Result<?> saveShoucang(@RequestBody NewsShoucang newssc) {
        newssc.setCreated(new Date());
        newsShoucangMapper.insert(newssc);
        return Result.success();
    }


    @GetMapping("/catList")
    public Result<?> catList() {
        return Result.success(NewsTypeEnum.getAll());
    }

    @PostMapping
    public Result<?> save(@RequestBody News news) {
        if(news.getCatId()!=null){
            news.setCatName(NewsCatEnum.getCatlog(news.getCatId()));
        }
        if(news.getType()!=null){
            news.setTypeName(NewsTypeEnum.getType(news.getType()));
        }
        news.setReadCounts(0);
        news.setNewsStatus(0);//待审核
        news.setTime(new Date());
        newsMapper.insert(news);
        return Result.success();
    }
    @PostMapping("/updateStatusDown")
    public Result<?> updateStatusDown(@RequestBody News news) {
        news.setNewsStatus(2);
        newsMapper.updateStatus(news.getNewsStatus(),news.getId());
        return Result.success();
    }
    @PostMapping("/updateStatusUp")
    public Result<?> updateStatusUp(@RequestBody News news) {
        news.setNewsStatus(1);
        newsMapper.updateStatus(news.getNewsStatus(),news.getId());
        return Result.success();
    }
    @PutMapping
    public Result<?> update(@RequestBody News news) {
        if(news.getCatId()!=null){
            news.setCatName(NewsCatEnum.getCatlog(news.getCatId()));
        }
        if(news.getType()!=null){
            news.setTypeName(NewsTypeEnum.getType(news.getType()));
        }
        newsMapper.updateById(news);
        return Result.success();
    }

    @GetMapping("/deleteById/{id}")
    public Result<?> deleteById(@PathVariable Long id) {
        newsMapper.deleteById(id);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<?> update(@PathVariable Long id) {
        newsMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        //增加阅读次数
        News news = newsMapper.selectById(id);
        int readc = 1;
        if(news == null){
            return Result.success(news);
        }
        if(news.getReadCounts()!=null){
            readc = readc + news.getReadCounts();
        }
        news.setReadCounts(readc);
        newsMapper.updateById(news);
        news.setDefaultImage(news.getDefaultImage().replace("localhost",Constants.DEFAULT_IP));
        news.setContent(news.getContent().replaceAll("localhost",Constants.DEFAULT_IP));
        if (StringUtils.isNotBlank(news.getDefaultVideo())) {
            news.setDefaultVideo(news.getDefaultVideo().replace("localhost",Constants.DEFAULT_IP));
        }
        return Result.success(news);
    }

    @GetMapping("/getById")
    public Result<?> getById(@RequestParam int id,@RequestParam int memberId) {
        //增加阅读次数
        News news = newsMapper.selectById(id);
        int readc = 1;
        if(news == null){
            return Result.success(news);
        }
        if(news.getReadCounts()!=null){
            readc = readc + news.getReadCounts();
        }
        news.setReadCounts(readc);
        newsMapper.updateById(news);
        news.setDefaultImage(news.getDefaultImage().replace("localhost",Constants.DEFAULT_IP));
        news.setContent(news.getContent().replaceAll("localhost",Constants.DEFAULT_IP));
        if(StringUtils.isNotBlank(news.getDefaultVideo())){
            news.setDefaultVideo(news.getDefaultVideo().replace("localhost",Constants.DEFAULT_IP));
        }

        //根据memberId和详情id 查询是否收藏
        LambdaQueryWrapper<NewsShoucang> wrapper = Wrappers.<NewsShoucang>lambdaQuery();
        wrapper.eq(NewsShoucang::getMemberId,memberId);
        wrapper.eq(NewsShoucang::getNewsId,id);
        Integer count = newsShoucangMapper.selectCount(wrapper);
        if(count!=null && count>0){
            news.setShoucang(1);//已经收藏过
        }
        else{
            news.setShoucang(0);
        }
        return Result.success(news);
    }


    @GetMapping("/indexQuery")
    public Result<?> indexQuery(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(defaultValue = "") String search,
                                @RequestParam(required = false) Integer catId,
                                @RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<News> wrapper = Wrappers.<News>lambdaQuery();
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(News::getTitle, search);
        }
        if(catId!=null){
            wrapper.eq(News::getCatId,catId);
        }
        if(type!=null){
            wrapper.eq(News::getType,type);
        }
        wrapper.eq(News::getNewsStatus,1);//审核通过的
        wrapper.orderByDesc(News::getId);
        Page<News> newsPage = newsMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<News> news = newsPage.getRecords();
        news.stream().forEach(obj->obj.setNewsStatusStr(getStatusStr(obj.getNewsStatus())));
        news.stream().forEach(obj->obj.setDefaultImage(obj.getDefaultImage().replace("localhost",Constants.DEFAULT_IP)));
        news.stream().forEach(obj->obj.setContent(obj.getContent().replaceAll("localhost",Constants.DEFAULT_IP)));
        for(int i=0;i<news.size();i++){
            News obj = new News();
            if(StringUtils.isNotBlank(obj.getDefaultVideo())){
                obj.setDefaultVideo(obj.getDefaultVideo().replace("localhost",Constants.DEFAULT_IP));
            }
        }
        return Result.success(newsPage);
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(required = false) Integer catId) {
        LambdaQueryWrapper<News> wrapper = Wrappers.<News>lambdaQuery();
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(News::getTitle, search);
        }
        if(catId!=null){
            wrapper.eq(News::getCatId,catId);
        }
        wrapper.orderByDesc(News::getId);
        Page<News> newsPage = newsMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<News> news = newsPage.getRecords();
        news.stream().forEach(obj->obj.setNewsStatusStr(getStatusStr(obj.getNewsStatus())));
        return Result.success(newsPage);
    }
    static String getStatusStr(int status){
        if(status == 1){
            return "上线中";
        }else if(status == 2){
            return  "下线中";
        }else{
            return "审核中";
        }

    }
}
