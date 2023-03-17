
package com.xmind;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
public class XmindAnalysis {

    public static List<XmindFile> xmindJSONToStringList = new ArrayList<>();

    public static String smokingFlag = "冒烟";

    private static String xpathSeparator = "/"; // 目录之间连接符

    private static final int hierarchy = 2; // 默认固定两层,需要时调整， hierarchy 为主题之下几层子主题，对应主目录下几层子目录

    private static String sourceFileName = "content.json";

    static {
        // (*^▽^*)
        File[] xmindFileSourceList = new File("./xfiles/source").listFiles((dir, name) -> name.endsWith(".xmind"));
        if (xmindFileSourceList == null || xmindFileSourceList.length == 0){
            throw new RuntimeException("请检查源目录是否存着和源目录下是否有需要解析的Xmind文件...");
        }
        for (File xmindFileSource : xmindFileSourceList){
            XmindFile xfile = new XmindFile();
            String[] fileNames = xmindFileSource.getName().split("\\.");
            fileNames = Arrays.copyOf(fileNames, fileNames.length-1);
            StringBuilder fileName = new StringBuilder();
            for (String s : fileNames){
                fileName.append(s).append(".");
            }
            fileName.append("csv");
            String xmindJSONToString = "";
            try {
                ZipFile zipFile = new ZipFile(xmindFileSource);
                Enumeration<?> entries = zipFile.entries();
                while (entries.hasMoreElements()){
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (entry.getName().equals(sourceFileName)){
                        xmindJSONToString = IOUtils.toString(new BufferedInputStream(zipFile.getInputStream(entry)),"UTF-8");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(System.out.append("解压出现问题请检查Xmind文件"));
            }

            if (!(xmindJSONToString.length() > 0)){
                try {
                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace(System.out.append("请检查Xmind文件解压出的JSON文件结果"));
                }
            }
            xfile.setBody(xmindJSONToString);
            xfile.setName(fileName.toString());
            xmindJSONToStringList.add(xfile);
        }
    }

    public static void main(String[] args) {
        // 开始处理文件
        for (XmindFile xmindFile : xmindJSONToStringList){
            analysisXmindTOResult(xmindFile);
        }
    }

    public static <R, P> void analysisXmindTOResult(XmindFile xmindFile){
        JSONArray array = JSON.parseArray(xmindFile.getBody());
        List<XmindStep> resultSteps = new ArrayList<>();
        for (Object o: array){
            XmindFrame<Xmind, R, P> xmindFrame = JSON.parseObject(JSON.toJSONString(o), new TypeReference<XmindFrame<Xmind, R, P>>(){});
            Xmind xmind = xmindFrame.getRootTopic();
            String topicTitle = xmind.getTitle();
            List<XmindStep> stepList = convertToBaseAction(xmind,new ArrayList<>());

//            String canvasName = xmindFrame.getTitle();
            // HierarchyState.valueOf("ONE").flag
//            HierarchyState.getFlag(canvasName);

            List<XmindStep> xmindCanvasSteps = handleSteps(stepList, topicTitle);
            resultSteps.addAll(xmindCanvasSteps);
            // 优化用例标题的获取方式ing
            // List<XmindHead> OptimizeTesting = convertToCaseTitle(xmind, new ArrayList<>());
        }
        String fileName = "./xfiles/target/" + xmindFile.getName();
        toFile(resultSteps, fileName);

    }

    public static List<XmindStep> handleSteps(List<XmindStep> stepList, String topicTitle){

        for (int i = 0; i < stepList.size(); i++){
            stepList.get(i).setSort(i);
            stepList.get(i).setCaseStoreyFlag(i);
        }

        // 筛选用例标识记录
        List<XmindStep> caseXmindStep = stepList.stream().filter(s -> StringUtils.isBlank(s.getStep()) && StringUtils.isBlank(s.getExpectedResult()) && StringUtils.isNotBlank(s.getTitle())).collect(Collectors.toList());

        // 筛选记录标识记录
        List<XmindStep> sourceXmindStep = stepList.stream().filter(s -> StringUtils.isNotBlank(s.getStep()) && StringUtils.isNotBlank(s.getExpectedResult())).collect(Collectors.toList());

        // (*^▽^*) 排序相减 提供确认层级的最初标识
        for (int i = 0; i < caseXmindStep.size(); i++){
            caseXmindStep.get(i).setCaseStoreyFlag(caseXmindStep.get(i).getCaseStoreyFlag()-i);
        }

        // 拼接用例标题
        Map<Integer, List<XmindStep>> mapXmindStep = caseXmindStep.stream().filter(xmindStep -> !xmindStep.getTitle().equals(topicTitle) ).collect(Collectors.groupingBy(XmindStep::getCaseStoreyFlag));
        TreeMap sortmap = new TreeMap(mapXmindStep);

        Iterator<Map.Entry<Integer, List<XmindStep>>> iterator = sortmap.entrySet().iterator();
        List<XmindStep> caseXmindStepList = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<XmindStep>> next = iterator.next();
            for(int s = 0; s < next.getValue().size(); s++){
                XmindStep step = next.getValue().get(s);
                step.setFlag(s+1);
                caseXmindStepList.add(step);
            }
            int r = next.getValue().size();
            // 涉及到层级重叠的做层级合并
            for (int is = 1; is < r; is++) {
                String title = next.getValue().get(is).getTitle();
                int ist = is + 1;
                caseXmindStepList.stream().filter(xmindStep -> xmindStep.getFlag() < ist).forEach(xmindStep -> {
                    xmindStep.setTitle(title + "-" + xmindStep.getTitle());
                    xmindStep.setFlag(xmindStep.getFlag() + 1);
                });
            }
        }

        // 将用例标题拼接至用例
        Map<Integer, List<XmindStep>> tmepXmindStep = caseXmindStepList.stream().collect(Collectors.groupingBy(XmindStep::getCaseStoreyFlag));
        TreeMap tmepSortMap = new TreeMap(tmepXmindStep);
        Iterator<Map.Entry<Integer, List<XmindStep>>> tmepIterator = tmepSortMap.entrySet().iterator();

        int it = -1;
        while (tmepIterator.hasNext()){
            Map.Entry<Integer, List<XmindStep>> next = tmepIterator.next();
            int is = next.getValue().get(0).getSort();
            for (XmindStep xmindStep : sourceXmindStep){
                if (xmindStep.getSort() < is && xmindStep.getSort() > it){
                    xmindStep.setTitle(next.getValue().get(0).getTitle());
                }
            }
            it = is;
        }

        // 如果全路径（路径+标题）是一样的就认为是一条测试用例 按要求将步骤合并为用例（具体区分用例在业务端猜测是用CaseId区分）
        Map<String, List<XmindStep>> tmepCase = sourceXmindStep.stream().filter(step->step.getTitle() != null).collect(Collectors.groupingBy(XmindStep::getTitle));
        TreeMap sortTmepCase = new TreeMap(tmepCase);
        Iterator<Map.Entry<String, List<XmindStep>>> varIterator = sortTmepCase.entrySet().iterator();

        int tmepNum = 0;
        while (varIterator.hasNext()){
            tmepNum+=1;
            Map.Entry<String, List<XmindStep>> next = varIterator.next();
            List<XmindStep> tmep = next.getValue();
            for (XmindStep xmindStep : tmep){
                xmindStep.setCaseId(tmepNum);
            }
        }

        // 格式化用例步骤 提供业务需要的用例格式
        List<XmindStep> formatSteps = new ArrayList<>();
        tmepCase.values().forEach(steps -> {
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
                stept.setPath(resultPath);
                formatSteps.add(stept);
            });
        });
        List<XmindStep> resultSteps = formatSteps.stream().sorted(Comparator.comparing(XmindStep::getCaseId)).collect(Collectors.toList());
        return resultSteps;

    }

    public static void toFile(List<XmindStep> steps, String fileName){
        String firstLine = XrayCase.getFormatNames() + System.getProperty("line.separator");
        try {
            FileUtils.writeStringToFile(new File(fileName), firstLine, "UTF-8");
            FileUtils.writeLines(new File(fileName), steps,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 调试的代码
        System.out.print(firstLine);
        for (XmindStep s: steps){
            System.out.println(s);
        }
    }

    /**
     * @param xmind Xmind 对象
     * @param steps Xmind 步骤集合
     * @return 将XMind转为以步骤为基准的记录
     * */
    public static List<XmindStep> convertToBaseAction(Xmind xmind, List<XmindStep> steps){
        List<Xmind> xmindList = xmind.getChildren().getAttached();
        XmindStep step = new XmindStep();
        StringBuilder sb = new StringBuilder();
        for (Xmind var: xmindList){
            if (var.getChildren() != null){
                convertToBaseAction(var,steps);
            }else {
                sb.append(var.getTitle());
            }
        }
        steps.add(step);
        if (sb.toString().length() > 0){
            step.setStep(xmind.getTitle());
            step.setExpectedResult(sb.toString());
        }else {
            String title = xmind.getTitle();
            step.setTitle(title);
        }
        return steps;
    }

    /**
     * 优化获取Title
     * */
    public static List<XmindHead> convertToCaseTitle(Xmind xmind, List<XmindHead> titles){
        List<Xmind> xmindList = xmind.getChildren().getAttached();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(xmind.getTitle());
        // 每个循环是一层
        for (Xmind xm: xmindList){
            XmindHead head = new XmindHead();
            titles.add(head);
            head.setTitle(stringBuilder.toString());

            if (xm.getChildren() != null){
                stringBuilder.append(xm.getTitle());
                head.setTitle(stringBuilder.toString());
                List<Xmind> list = xm.getChildren().getAttached();
                boolean flag = true;
                for (Xmind x: list){
                    if (x.getChildren() != null){
                        convertToCaseTitle(x, titles);
                    }else {
                        if (flag){
                            titles.remove(titles.size()-1);
                            flag = false;
                            break;
                        }
                    }
                }
            }
            // TODO
        }
        return titles;
    }

    public static List<XmindHead> convertToCaseTitleTest(Xmind xmind, List<XmindHead> titles){
        List<Xmind> xmindList = xmind.getChildren().getAttached();
        XmindHead head = new XmindHead();
        head.setTitle(xmind.getTitle());
        titles.add(head);
        boolean flag = true;
        // 每个循环是一层
        for (Xmind x: xmindList){
            if (x.getChildren() != null){
                convertToCaseTitleTest(x, titles);
            }else {
                if (flag){
                    titles.remove(titles.size()-1);
                    flag = false;
                    break;
                }
            }
        }
        return titles;
    }

}



