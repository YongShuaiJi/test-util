package org.xmind.transform.execute;

import org.xmind.transform.dto.Xmind;
import org.xmind.transform.dto.XmindStep;

import java.util.List;
import java.util.Optional;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2023/3/22
 */
public class AnalysisCore {

    /**
     * 进一步优化，获取步骤后先获取title再循环：
     * @param xmind Xmind 起始对象程序解析的数据源
     * @param steps Xmind 步骤集合，程序存储的结果极氪
     * @param builders 用于步骤间标题的传递
     * @return 将XMind转为以步骤为基准的记录
     * */
    public List<XmindStep> convertToBaseAction(Xmind xmind, List<XmindStep> steps, StringBuilder builders){
        List<Xmind> xmindList = xmind.getChildren().getAttached();
        XmindStep step = new XmindStep();
        builders.append(Optional.ofNullable(xmind.getTitle()).orElse("")).append("-");
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (Xmind var: xmindList){
            if (var.getChildren() != null){
                convertToBaseAction(var, steps, builders);
            }else {
                sb.append(var.getTitle());
                sb.append(System.getProperty("line.separator")); // 优化：预期结果换行
                if (flag){
                    builders.replace(builders.length() - (Optional.ofNullable(xmind.getTitle()).orElse("").length() + 1), builders.length(),"");
                    flag = false;
                }
            }
        }
        if (!sb.toString().isEmpty()){
            steps.add(step);
            step.setStep(Optional.ofNullable(xmind.getTitle()).orElse(""));
            step.setExpectedResult(sb.toString());
            try {
                step.setTitle(builders.substring(0, builders.length()-1));
            }catch (Exception e){
                throw new RuntimeException("请检查每条记录的层级是否足够，每条记录至少要保证三个节点");
            }
        }else {
            // 值得比较的内容-优化：只对用例标题内容进行比较
            if (builders.length() > Optional.ofNullable(xmind.getTitle()).orElse("").length()
                    && Optional.ofNullable(xmind.getTitle()).orElse("").equals(builders.substring(builders.length() - (Optional.ofNullable(xmind.getTitle()).orElse("").length() + 1), builders.length()-1))){
                builders.replace(builders.length() - (Optional.ofNullable(xmind.getTitle()).orElse("").length() + 1), builders.length(),"");
            }
        }

        return steps;
    }

}
