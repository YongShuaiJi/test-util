package org.analyse.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2023/04/06 23:15:56
 */
public class ContinuousString {

    /**
     * 问题应该是一个比较常见的问题，可能还有更好的方法解决
     * 问题：查找摘录不重复的最长连续字符串
     * 写这个方法是某次之说了自己解决问题的思路，没有快速写完，再次补全了
     * 解决方法：
     * 双重FOR循环，从前往后走，从后往前遍历
     * 核心：框出目标范围
     * */
    public static void main(String[] args) {
        String s = "abcccccsdfioqccabc";
        String[] target = s.split("");
        List<String> storage = new ArrayList<>();
        StringBuilder transformable  = new StringBuilder();
        int temp = 0;
        for (int i = 0; i < target.length; i++){
            // 记录不重复的元素
            transformable.append(target[i]);
            for(int j = i-1; j >= temp; j--){
                if (target[j].equals(target[i])){
                    // 元素重复时需要记录不重复的连续字符串并清空公共数据池
                    storage.add(transformable.substring(0, transformable.length()-1));
                    transformable = new StringBuilder();
                    // 界定内层的最大范围和外层下一轮的起始位置 - 这不就可以标定出来了(*^▽^*)
                    temp = i;
                    i = j;
                    break;
                }
            }
        }
        storage.add(transformable.toString());
        System.out.println(storage);
        // (*^▽^*) 获取最长不重复连续字符串，可能有多个
        List result = storage.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.groupingBy(String::length)).
                get(storage.stream().max(Comparator.comparingInt(String::length)).get().length());
        System.out.println(result);
//        Map<Integer, List<String>> map = storage.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.groupingBy(String::length));
//        List result2 = map.get(map.keySet().stream().max(Integer::compareTo).get());
    }
}
