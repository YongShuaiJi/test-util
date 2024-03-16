package org.xmind.transform.enums;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
public enum XrayCase {
    CASEID("用例ID","Test Case Identifier"),
    SUMMARY("概要","summary"),
    PRIORITY("优先级","priority"),
    LABELS("标签","labels"),
    DATA("数据","Data"),
    ACTION("操作","Action"),
    EXPECTED("预期","Expected Result"),
    CUSTOMFIEID10411("目录","customfield_10411");
    private final String name;
    private final String flag;
    XrayCase(String name, String flag) {
        this.name = name;
        this.flag = flag;
    }
    public String getName(){
        return name;
    }
    public String getFlag(){
        return flag;
    }
    public static String getFormatNames(){
        XrayCase[] cases = XrayCase.values();
        StringBuilder bb = new StringBuilder();
        for (XrayCase xrayCase: cases){
            bb.append(xrayCase.getName()).append(";");
        }
        return bb.substring(0, bb.length()-1);
    }
}

