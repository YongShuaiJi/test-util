package org.xmind.transform.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
@Getter
@Setter
@ToString
public class XMindStep {
    private int caseId;
    // 目标程序中的用例标题字符数最大长度255
    @Size(max = 255, message = "用例标题过长，超过255个字符")
    private String title;
    private String priority = "P1";
    private String label = "";
    private String data = "";
    private String step;
    private String expectedResult;
    private String path;
    private int sort;
    /**
     * 用例层级标识
     * 用来合并多层主题
     * */
    private int caseStoreyFlag;
    private int flag;

    @Override
    public String toString() {
        if (title != null){
            title = title.replace("\"","'");
        }
        if (step != null){
            step = step.replace("\"","'");
        }
        if (expectedResult != null){
            expectedResult = expectedResult.replace("\"","'");
        }
        if (path != null){
            path = path.replace("\"","'");
        }

        return  caseId +
                ";" + valueOf(title) +
                ";" + valueOf(priority) +
                ";" + valueOf(label) +
                ";" + valueOf(data) +
                ";" + "\"" + valueOf(step) +"\"" +
                ";" + "\"" + valueOf(expectedResult) + "\"" +
                ";" + valueOf(path);
    }

    public String valueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }
}
