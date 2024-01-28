package org.xmind.transform.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
@Getter
@Setter
@ToString
@Component
public class XMindStep {
    @ExcelProperty("用例ID")
    private int caseId;
    // 目标程序中的用例标题字符数最大长度255
    @ExcelProperty("用例标题")
    @Size(max = 255, message = "用例标题过长，超过255个字符")
    private String title;
    @ExcelProperty("优先级")
    private String priority = "P1";
    @ExcelIgnore
    private String label = "";
    @ExcelIgnore
    private String data = "";
    @ExcelProperty("操作步骤")
    private String step;
    @ExcelProperty("执行结果")
    private String expectedResult;
    @ExcelProperty("路径")
    private String path;
    @ExcelIgnore
    private int sort;
    /**
     * 用例层级标识
     * 用来合并多层主题
     * */
    @ExcelIgnore
    private int caseStoreyFlag;
    @ExcelIgnore
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
