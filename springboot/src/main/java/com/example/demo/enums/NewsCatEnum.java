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
public enum NewsCatEnum {


//    SALES_ALL(-1,"全部分类"),
    SALES_SIGN(1, "图片"),
    SALES_IN(2, "视频"),
    SALES_EXECUTE(3, "文字");//M23其他

    private Integer id;
    private String name;
    private static final Set<NewsCatEnum> ALL = EnumSet.allOf(NewsCatEnum.class);


    public static String getCatlog(Integer id) {
        return ALL.stream()
                .filter(o -> o.id.equals(id))
                .map(o -> o.getName())
                .findAny().orElse(null);
    }
    public static List<Catlog> getAll() {
        List<Catlog> catlogList = new ArrayList<>();
        for(NewsCatEnum newsCatEnum:ALL){
            Catlog catlog = new Catlog();
            catlog.setId(newsCatEnum.getId());
            catlog.setName(getCatlog(newsCatEnum.getId()));
            catlogList.add(catlog);
        }
        return catlogList;
    }
}
@Data
class Catlog{
    Integer id;
    String name;
}
