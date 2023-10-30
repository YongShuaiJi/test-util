package org.xmind.transform.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2023-10-30 21:57:06
 */
public enum Priority {

    P0("P0"),
    P1("P1"),
    P2("P2"),
    P3("P3"),
    P4("P4");

    private String key;

    Priority(String key) {
        this.key = key;
    }

    public static String getKey(Priority priority){
        return priority.key;
    }

    public static Priority getPriority(String key){
        for (Priority priority : Priority.values()){
            if (priority.key.equals(key)){
                return priority;
            }
        }
        return null;
    }

    public static List<String> getKeys(){
        List<String> keys = new ArrayList<>();
        for (Priority priority : Priority.values()){
            keys.add(priority.key);
        }
        return keys;
    }

}
