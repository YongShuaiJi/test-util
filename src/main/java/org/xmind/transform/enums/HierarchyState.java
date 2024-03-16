package org.xmind.transform.enums;
import java.util.ArrayList;
import java.util.List;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
/**
 * hierarchy 为主题之下几层子主题，对应主目录下几层子目录
 * */
public enum HierarchyState {

    ZERO(0, "zero"),
    ONE(1, "one"),
    TWO(2, "two"),
    THREE(3, "three"),
    FOUR(4, "four"),
    Five(5,"five"),
    SIX(6, "six"),
    SEVEN(7,"seven"),
    DEFAULT(2,"default");

    private final int code;

    private final String name;

    HierarchyState(int code, String name){
        this.code = code;
        this.name = name;
    }

    public static List<String> getHierarchyNames(){
        List<String> names = new ArrayList<>();
        for (HierarchyState hierarchy : HierarchyState.values()){
            names.add(hierarchy.name);
        }
        return names;
    }

    public static int getCode(String name){
        // 默认固定两层
        for (HierarchyState state: HierarchyState.values()){
            if (state.name.equals(name)){
                return state.code;
            }
        }
        return HierarchyState.valueOf("DEFAULT").code;
    }

}
