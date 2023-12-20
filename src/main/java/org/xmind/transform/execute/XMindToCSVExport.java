package org.xmind.transform.execute;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.io.FileUtils;
import org.xmind.transform.config.baseConfig;
import org.xmind.transform.enums.HierarchyState;
import org.xmind.transform.dto.XMindStep;
import org.xmind.transform.enums.Priority;
import org.xmind.transform.enums.XrayCase;
import org.xmind.transform.dto.XMind;
import org.xmind.transform.dto.XMindFile;
import org.xmind.transform.dto.XMindFrame;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2023/3/22
 */
public class XMindToCSVExport<R, P> extends baseConfig implements XMindExportStrategy<XMindFile> {

    private AnalysisCore analysisCore = new AnalysisCore();

    @Override
    public void execute(XMindFile xmindFile) {
        List<JSONObject> array = JSON.parseArray(xmindFile.getBody(), JSONObject.class);
        List<XMindStep> resultSteps = new ArrayList<>();
        for (JSONObject o: array){
            XMindFrame<XMind, R, P> xmindFrame = JSON.parseObject(o.toString(), new TypeReference<XMindFrame<XMind, R, P>>(){});
            XMind xmind = xmindFrame.getRootTopic();
            List<XMindStep> stepList = analysisCore.convertToBaseAction(xmind, new ArrayList<>(), new StringBuilder());
            String topicTitle = xmind.getTitle();
            // 修剪一下用例标题-美观需求:去除用例标题中的主标题
            stepList.forEach(step -> {
                String  title = step.getTitle();
                if (title != null && title.length() >=title.length() && title.substring(0, topicTitle.length()).equals(topicTitle) && !title.equals(topicTitle)){
                    title = title.substring(topicTitle.length()+1, title.length());
                }
                step.setTitle(title);
            });
            // hierarchy 层级赋值每个画布不一样
            int hierarchy = HierarchyState.getCode(xmindFrame.getTitle());
            List<XMindStep> xmindCanvasSteps = handleXmindSteps(stepList, topicTitle, hierarchy);
            resultSteps.addAll(xmindCanvasSteps);
        }
        String fileName = targetPath + xmindFile.getName();
        toFile(resultSteps, fileName);
    }

    /**
     * 基础步骤的格式化 给需要的属性赋值
     * @param stepList 基础数据源
     * @param topicTitle 画布主题
     * @return  格式化基准步骤
     * */
    private List<XMindStep> handleXmindSteps(List<XMindStep> stepList, String topicTitle, int hierarchy){
        Map<String, List<XMindStep>> mapStep = stepList.stream().collect(Collectors.groupingBy(XMindStep::getTitle));
        TreeMap stepSortMap = new TreeMap(mapStep);
        Iterator<Map.Entry<String, List<XMindStep>>> stepIterator = stepSortMap.entrySet().iterator();
        // setCaseId
        int tmepNum = 0;
        while (stepIterator.hasNext()){
            tmepNum+=1;
            Map.Entry<String, List<XMindStep>> next = stepIterator.next();
            for (XMindStep xmindStep : next.getValue()){
                xmindStep.setCaseId(tmepNum);
            }
        }
        // 格式化用例步骤 提供业务需要的用例格式
        List<XMindStep> formatSteps = new ArrayList<>();
        mapStep.values().forEach(steps -> {
            steps.forEach(stept->{
                String[] titles = stept.getTitle().split("-");
                if (titles.length < hierarchy){
                    try {
                        throw new Exception();
                    } catch (Exception e) {
                        e.printStackTrace(System.out.append("实际Xmind层级少于预期层级,请检查Xmind文件层级与hierarchy是否相符"));
                    }
                }
                StringBuilder s = new StringBuilder(topicTitle);
                for (int i = 0; i < hierarchy; i++){
                    s.append(xpathSeparator).append(titles[i]);
                }
                String resultPath = s.toString();
                // 通过冒烟标识确定冒烟用例
                if (stept.getTitle().contains(smokingFlag)){
                    stept.setPriority("P0");
                }
                // 识别用例优先级
                for (String priorityKey : Priority.getKeys()){
                    if (stept.getTitle().contains(priorityKey)){
                        stept.setTitle(stept.getTitle().replace(priorityKey, ""));
                        stept.setPriority(priorityKey);
                    }
                }
                // 去除因为空元素块带来的 -
                String[] target = stept.getTitle().split("-");
                String targe = "";
                for (String title : target){
                    if (!title.isEmpty()){
                        targe = targe + title + '-';
                    }
                }
                targe = targe.substring(0, targe.length()-1);
                stept.setTitle(targe);
                stept.setPath(resultPath);
                formatSteps.add(stept);
            });
        });
        List<XMindStep> resultSteps = formatSteps.stream().sorted(Comparator.comparing(XMindStep::getCaseId)).collect(Collectors.toList());
        return resultSteps;
    }

    /**
     * 将内容写入文件
     * @param steps 数据源
     * @param fileName 文件名称
     * */
    private void toFile(List<XMindStep> steps, String fileName){
        String firstLine = XrayCase.getFormatNames() + System.getProperty("line.separator");
        try {
            FileUtils.writeStringToFile(new File(fileName), firstLine, "UTF-8");
            FileUtils.writeLines(new File(fileName), steps,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 调试的代码
//        System.out.print(firstLine);
//        for (XMindStep s: steps){
//            System.out.println(s);
//        }
    }

}