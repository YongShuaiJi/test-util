package com.xmind;
import java.util.ArrayList;
import java.util.List;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
public enum HierarchyState {
    ONE(1, "one"),
    TWO(2, "two"),
    THREE(3, "three"),
    FOUR(4, "four"),
    Five(5,"five"),
    SIX(6, "six"),
    SEVEN(7,"seven");
    private int flag;
    private String name;
    HierarchyState(int flag, String name){
        this.flag = flag;
        this.name = name;
    }

    public static List<String> getHierarchyNames(){
        List<String> names = new ArrayList<>();
        for (HierarchyState hierarchy : HierarchyState.values()){
            names.add(hierarchy.name);
        }
        return names;
    }
    public static List<String> getFlag(String name){
        List<String> names = new ArrayList<>();
        for (HierarchyState hierarchy : HierarchyState.values()){
            names.add(hierarchy.name);
        }
        return names;
    }
}

