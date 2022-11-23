package com.example.demo.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@RestController
@RequestMapping("/newsMessage")
public class NewsMessageController extends BaseController{
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

    public static final String PATH = "../../../../../../../../NLUMulti1/model_save/proposed/Predict.py";

    @PostMapping("/updateStatusDown")
    public Result<?> updateStatusDown(@RequestBody NewsMessage newsMessage) {
        newsMessageMapper.updateStatus(0,newsMessage.getId());
        return Result.success();
    }
    @PostMapping("/updateStatusUp")
    public Result<?> updateStatusUp(@RequestBody NewsMessage newsMessage) {
        newsMessageMapper.updateStatus(1,newsMessage.getId());
        return Result.success();
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody NewsMessage Message) {
//        Message.setUsername(getUser().getUsername());
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

    // 查询所有数据
    @GetMapping("/foreign/{id}")
    public Result<?> foreign(@PathVariable Integer id) {
        return Result.success(findByForeign(id));
    }
    // 查询所有数据
    @GetMapping("/test2/{title}")
    public Result<?> test2(@PathVariable String title) {
        String zmdf = initZmdf(title);
        String fmdf = getFmdf(zmdf);
        Map rst = new HashMap();
        rst.put("zmdf",zmdf);
        rst.put("fmdf",fmdf);

        return Result.success(rst);
    }
    // 查询所有数据
    @GetMapping("/test/{id}")
    public Result<?> test(@PathVariable Integer id) {
        NewsMessage newsMessage = newsMessageMapper.selectById(id);
        String zmdf = initZmdf(newsMessage.getContent());
        String fmdf = getFmdf(zmdf);
        Map rst = new HashMap();
        rst.put("zmdf",zmdf);
        rst.put("fmdf",fmdf);

        return Result.success(rst);
    }

    @GetMapping("/page")
    public Result<?> findPage(@RequestParam(required = false, defaultValue = "1") Integer queryMstatus,
                              @RequestParam(required = false, defaultValue = "") String name,
                              @RequestParam(required = false, defaultValue = "") Integer newsId,
                                                @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(required = false, defaultValue = "10") Integer pageSize) throws InterruptedException {
        LambdaQueryWrapper<NewsMessage> query = Wrappers.<NewsMessage>lambdaQuery()
                        .like(NewsMessage::getContent, name).
                        orderByDesc(NewsMessage::getId);
        if(newsId != null){
            query.eq(NewsMessage::getNewsId,newsId);
        }

        if(queryMstatus!=null && queryMstatus!=-1){
            query.eq(NewsMessage::getMessageStatus,queryMstatus);
        }
        IPage<NewsMessage> pages = newsMessageMapper.selectPage(new Page<>(pageNum, pageSize),query);

        List<NewsMessage> list = pages.getRecords();

        BigDecimal t_zm = BigDecimal.ZERO;
        BigDecimal t_fm = BigDecimal.ZERO;

        Map rst = new HashMap<>();

        if(CollectionUtil.isNotEmpty(list)){
            // 循环所有留言数据
            for (NewsMessage Message : list) {
                News news_one = newsMapper.selectOne(Wrappers.<News>lambdaQuery().eq(News::getId, Message.getNewsId()));
                if(news_one != null){
                    Message.setTitle(news_one.getTitle());
                }

                Member one = memberMapper.selectOne(Wrappers.<Member>lambdaQuery().eq(Member::getUsername, Message.getUsername()));
                if (one!=null && StrUtil.isNotBlank(one.getAvatar())) {
                    Message.setAvatar(one.getAvatar());
                } else {
                    // 默认一个头像
                    Message.setAvatar("http://localhost:9090/files/preview/1.png");
                }
                Long parentId = Message.getParentId();

                String zmdf = initZmdf(Message.getContent());
                Thread.sleep(2000);
                String fmdf = getFmdf(zmdf);
                Message.setZmdf(zmdf);
                Message.setFmdf(fmdf);

                //计算总得分
                t_zm = getTotal(t_zm,zmdf);
                t_fm = getTotal(t_fm,fmdf);

                // 判断当前的留言是否有父级，如果有，则返回父级留言的信息
                // 原理：遍历所有留言数据，如果id跟当前留言信息的parentId相等，则将其设置为父级评论信息，也就是Message::setParentMessage
                list.stream().filter(c -> c.getId().equals(parentId)).findFirst().ifPresent(Message::setParentMessage);
            }

            rst.put("fmdf",t_fm.divide(BigDecimal.valueOf(list.size()),2,BigDecimal.ROUND_HALF_UP));
            rst.put("zmdf",t_zm.divide(BigDecimal.valueOf(list.size()),2,BigDecimal.ROUND_HALF_UP));
        }
        pages.setRecords(list);
        rst.put("page",pages);

        //正面
        if(t_zm.compareTo(t_fm) == 1){
            rst.put("zm",1);
        }
        else{
            rst.put("zm",0);
        }
        return Result.success(rst);
    }

    //TODO 替换成python模型
    String initZmdf(String str){
        if(str==null||str.length()==0){
            return "0";
        }
//        AttitudeResp pythonResult = pythonClient.getPythonResult(str);
//        if(Objects.isNull(pythonResult)){
//            System.out.println("调用python接口返回值为空！");
//            return "0";
//        }
        //http://localhost:9876/api/newsMessage/page?pageNum=1&pageSize=10&queryMstatus=0&search=
        //http://localhost:9876/api/newsMessage/page?pageNum=1&pageSize=5&search=&newsId=12
//        String score = pythonResult.getScore();
//         返回98.3213214
//         返回的是0.9992323
//        BigDecimal bigDecimal  = new BigDecimal(score);
        BigDecimal bigDecimal  = new BigDecimal(0.99923242);
        return bigDecimal.multiply(new BigDecimal(100)).setScale(2,BigDecimal.ROUND_UP).toString();
    }


//    public static void exePython(String params, AttitudeResp projectResp) throws IOException {
//        String[] command = new String[]{"python", "D:\\Bjfunews\\NLUMulti1\\model_save\\proposed\\Predict.py", params};
//        final Process process = Runtime.getRuntime().exec(command);
//        printMessage(process.getInputStream(), projectResp);
//    }
//
//    private static void printMessage(final InputStream input, AttitudeResp projectResp) {
//        new Thread(() -> {
//            Reader reader = new InputStreamReader(input);
//            BufferedReader bf = new BufferedReader(reader);
//            String line = null;
//            try {
//                AtomicInteger atomicInteger = new AtomicInteger(0);
//
//                while ((line = bf.readLine()) != null) {
//                    System.out.println(line); // 返回值
//                    int i = atomicInteger.incrementAndGet();
//                    if (i == 2) {
//                        projectResp.setAttitude(line);
//                    } else if (i == 3) {
//                        projectResp.setEmotion(line);
//                    }
//                }
//                reader.close();
//                bf.close();
//            } catch (IOException e) {
//
//                e.printStackTrace();
//
//            }
//        }).start();
//    }


    String getFmdf(String zmdf){
        BigDecimal z = new BigDecimal(zmdf).setScale(6,BigDecimal.ROUND_HALF_UP);
        BigDecimal f = BigDecimal.valueOf(100).subtract(z);
        return f.toString();
    }

    BigDecimal getTotal(BigDecimal start,String df){
        BigDecimal z = new BigDecimal(df).setScale(6,BigDecimal.ROUND_HALF_UP);
        return start.add(z);
    }

    @GetMapping("/myMessage")
    public Result<?> myMessage(@RequestParam(required = false, defaultValue = "") String memberName,
                              @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                              @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<NewsMessage> query = Wrappers.<NewsMessage>lambdaQuery().like(NewsMessage::getUsername, memberName).orderByDesc(NewsMessage::getId);

        IPage<NewsMessage> pages = newsMessageMapper.selectPage(new Page<>(pageNum, pageSize),query);

        return Result.success(pages);
    }

    private List<NewsMessage> findByForeign(Integer foreignId) {
        // 根据 foreignId 0 查询所有的留言数据
        LambdaQueryWrapper<NewsMessage> queryWrapper =
                Wrappers.<NewsMessage>lambdaQuery().eq(NewsMessage::getNewsId, foreignId).orderByDesc(NewsMessage::getId);
        List<NewsMessage> list = newsMessageMapper.selectList(queryWrapper);
        // 循环所有留言数据
        for (NewsMessage Message : list) {
            Member one = memberMapper.selectOne(Wrappers.<Member>lambdaQuery().eq(Member::getUsername, Message.getUsername()));
            if (StrUtil.isNotBlank(one.getAvatar())) {
                Message.setAvatar(one.getAvatar());
            } else {
                // 默认一个头像
                Message.setAvatar("http://localhost:9090/files/preview/1.png");
            }
            Long parentId = Message.getParentId();
            // 判断当前的留言是否有父级，如果有，则返回父级留言的信息
            // 原理：遍历所有留言数据，如果id跟当前留言信息的parentId相等，则将其设置为父级评论信息，也就是Message::setParentMessage
            list.stream().filter(c -> c.getId().equals(parentId)).findFirst().ifPresent(Message::setParentMessage);
        }
        return list;
    }


}
