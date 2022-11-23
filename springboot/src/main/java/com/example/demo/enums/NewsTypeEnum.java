package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NewsTypeEnum {


//    SALES_ALL(-1,"全部分类"),
    SALES_SIGN(1, "绿色要闻"),
    SALES_IN(2, "校园动态"),
    SALES_EXECUTE(3, "微媒体"),
    SALES_EXECUTE4(4, "媒体北林"),
    SALES_EXECUTE5(5, "党建思政"),
    SALES_EXECUTE6(6, "绿色人物"),
    SALES_EXECUTE7(7, "观点言论"),
    SALES_EXECUTE9(9, "重大活动"),
    SALES_EXECUTE10(10, "校园风光"),
    SALES_EXECUTE11(11, "作品展示"),
    SALES_EXECUTE12(12, "师生风采"),
    SALES_EXECUTE13(13, "历史图片"),
    SALES_EXECUTE14(14, "视频新闻"),
    SALES_EXECUTE15(15, "学校风采"),
    SALES_EXECUTE16(16, "学生作品"),
    SALES_EXECUTE17(17, "人物访谈"),
    SALES_EXECUTE18(18, "学习天地");


    private Integer id;
    private String name;
    private static final Set<NewsTypeEnum> ALL = EnumSet.allOf(NewsTypeEnum.class);


    public static String getType(Integer id) {
        return ALL.stream()
                .filter(o -> o.id.equals(id))
                .map(o -> o.getName())
                .findAny().orElse(null);
    }
    public static List<Catlog> getAll() {
        List<Catlog> catlogList = new ArrayList<>();
        for(NewsTypeEnum newsCatEnum:ALL){
            Catlog catlog = new Catlog();
            catlog.setId(newsCatEnum.getId());
            catlog.setName(getType(newsCatEnum.getId()));
            catlogList.add(catlog);
        }
        return catlogList;
    }
}

@Data
class Type{
    Integer id;
    String name;
}
