package org.xmind.transform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xmind.transform.dto.*;
import org.xmind.transform.enums.HierarchyState;
import org.xmind.transform.enums.XrayCase;

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
 * @updateDate 2023/3/20 21:51
 * 原始的类已弃用的类不在更新，用其他类重新实现此功能
 * */
@Deprecated
public class XMindAnalysis {

    public static List<XMindFile> xmindJSONToStringList = new ArrayList<>();

    public static String smokingFlag = "冒烟";

    private static String xpathSeparator = "/"; // 目录之间连接符

//    private static final int hierarchy = 2; // 默认固定两层,需要时调整， hierarchy 为主题之下几层子主题，对应主目录下几层子目录

    private static String sourceFileName = "content.json";


    static {
        // (*^▽^*)
        File[] xmindFileSourceList = new File("./xfiles/source").listFiles((dir, name) -> name.endsWith(".xmind"));
        for (File xmindFileSource : xmindFileSourceList){
            XMindFile xfile = new XMindFile();
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
        for (XMindFile xmindFile : xmindJSONToStringList){
            analysisXmindTOResult(xmindFile);
        }
    }

    /**
     * 解析XMind内容文件，将内容文件转化为格式化的记录
     * @param xmindFile XMind 文件对象
     * */
    public static <R, P> void analysisXmindTOResult(XMindFile xmindFile){
        List<JSONObject> array = JSON.parseArray(xmindFile.getBody(), JSONObject.class);
        List<XMindStep> resultSteps = new ArrayList<>();
        for (JSONObject o: array){
            XMindFrame<XMind, R, P> xmindFrame = JSON.parseObject(o.toString(), new TypeReference<XMindFrame<XMind, R, P>>(){});
            XMind xmind = xmindFrame.getRootTopic();
            String topicTitle = xmind.getTitle();
            List<XMindStep> stepList = convertToBaseAction(xmind,new ArrayList<>(), new StringBuilder());
            // 修剪一下用例标题-美观需求:去除用例标题中的主标题
            stepList.forEach(step -> {
                String  title = step.getTitle();
                if (title.substring(0, topicTitle.length()).equals(topicTitle) && !title.equals(topicTitle)){
                    title = title.substring(topicTitle.length()+1, title.length());
                }
                step.setTitle(title);
            });
            // hierarchy 层级赋值每个画布不一样
            int hierarchy = HierarchyState.getCode(xmindFrame.getTitle());
            List<XMindStep> xmindCanvasSteps = handleXmindSteps(stepList, topicTitle, hierarchy);
            resultSteps.addAll(xmindCanvasSteps);
        }
        String fileName = "./xfiles/target/" + xmindFile.getName();
        toFile(resultSteps, fileName);
    }

    /**
     * 最初的处理方式，用于处理 convertToBaseAction(XMind xmind, List<XMindStep> steps) 方法计算出来的数据集合
     * 当前有了更好的计算方式可以把基础数据集合算的更加准确。
     * */
    @Deprecated
    private static List<XMindStep> handleSteps(List<XMindStep> stepList, String topicTitle, int hierarchy){

        for (int i = 0; i < stepList.size(); i++){
            stepList.get(i).setSort(i);
            stepList.get(i).setCaseStoreyFlag(i);
        }

        // 筛选用例标识记录
        List<XMindStep> caseXmindStep = stepList.stream().filter(s -> StringUtils.isBlank(s.getStep()) && StringUtils.isBlank(s.getExpectedResult()) && StringUtils.isNotBlank(s.getTitle())).collect(Collectors.toList());

        // 筛选记录标识记录
        List<XMindStep> sourceXmindStep = stepList.stream().filter(s -> StringUtils.isNotBlank(s.getStep()) && StringUtils.isNotBlank(s.getExpectedResult())).collect(Collectors.toList());

        // (*^▽^*) 排序相减 提供确认层级的最初标识
        for (int i = 0; i < caseXmindStep.size(); i++){
            caseXmindStep.get(i).setCaseStoreyFlag(caseXmindStep.get(i).getCaseStoreyFlag()-i);
        }

        // 拼接用例标题
        Map<Integer, List<XMindStep>> mapXmindStep = caseXmindStep.stream().filter(xmindStep -> !xmindStep.getTitle().equals(topicTitle) ).collect(Collectors.groupingBy(XMindStep::getCaseStoreyFlag));
        TreeMap sortmap = new TreeMap(mapXmindStep);

        Iterator<Map.Entry<Integer, List<XMindStep>>> iterator = sortmap.entrySet().iterator();
        List<XMindStep> caseXmindStepList = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<XMindStep>> next = iterator.next();
            for(int s = 0; s < next.getValue().size(); s++){
                XMindStep step = next.getValue().get(s);
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
        Map<Integer, List<XMindStep>> tmepXmindStep = caseXmindStepList.stream().collect(Collectors.groupingBy(XMindStep::getCaseStoreyFlag));
        TreeMap tmepSortMap = new TreeMap(tmepXmindStep);
        Iterator<Map.Entry<Integer, List<XMindStep>>> tmepIterator = tmepSortMap.entrySet().iterator();

        int it = -1;
        while (tmepIterator.hasNext()){
            Map.Entry<Integer, List<XMindStep>> next = tmepIterator.next();
            int is = next.getValue().get(0).getSort();
            for (XMindStep xmindStep : sourceXmindStep){
                if (xmindStep.getSort() < is && xmindStep.getSort() > it){
                    xmindStep.setTitle(next.getValue().get(0).getTitle());
                }
            }
            it = is;
        }

        // 如果全路径（路径+标题）是一样的就认为是一条测试用例 按要求将步骤合并为用例（具体区分用例在业务端猜测是用CaseId区分）
        Map<String, List<XMindStep>> tmepCase = sourceXmindStep.stream().collect(Collectors.groupingBy(XMindStep::getTitle));
        TreeMap sortTmepCase = new TreeMap(tmepCase);
        Iterator<Map.Entry<String, List<XMindStep>>> varIterator = sortTmepCase.entrySet().iterator();

        int tmepNum = 0;
        while (varIterator.hasNext()){
            tmepNum+=1;
            Map.Entry<String, List<XMindStep>> next = varIterator.next();
            List<XMindStep> tmep = next.getValue();
            for (XMindStep xmindStep : tmep){
                xmindStep.setCaseId(tmepNum);
            }
        }

        // 格式化用例步骤 提供业务需要的用例格式
        List<XMindStep> formatSteps = new ArrayList<>();
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
        List<XMindStep> resultSteps = formatSteps.stream().sorted(Comparator.comparing(XMindStep::getCaseId)).collect(Collectors.toList());
        return resultSteps;

    }

    /**
     * 基础步骤的格式化 给需要的属性赋值
     * @param stepList 基础数据源
     * @param topicTitle 画布主题
     * @return  格式化基准步骤
     * */
    private static List<XMindStep> handleXmindSteps(List<XMindStep> stepList, String topicTitle, int hierarchy){
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
    private static void toFile(List<XMindStep> steps, String fileName){
        String firstLine = XrayCase.getFormatNames() + System.getProperty("line.separator");
        try {
            FileUtils.writeStringToFile(new File(fileName), firstLine, "UTF-8");
            FileUtils.writeLines(new File(fileName), steps,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 调试的代码
        System.out.print(firstLine);
        for (XMindStep s: steps){
            System.out.println(s);
        }
    }


    /**
     * 进一步优化，获取步骤后先获取title再循环：
     * @param xmind XMind 起始对象程序解析的数据源
     * @param steps XMind 步骤集合，程序存储的结果
     * @param builders 用于步骤间标题的传递
     * @return 将XMind转为以步骤为基准的记录
     * */
    private static List<XMindStep> convertToBaseAction(XMind xmind, List<XMindStep> steps, StringBuilder builders){
        List<XMind> xmindList = xmind.getChildren().getAttached();
        XMindStep step = new XMindStep();
        builders.append(xmind.getTitle()).append("-");
        StringBuilder sb = new StringBuilder();

        boolean flag = true;
        for (XMind var: xmindList){
            if (var.getChildren() != null){
                convertToBaseAction(var, steps, builders);
            }else {
                sb.append(var.getTitle());
                if (flag){
                    builders.replace(builders.length() - (xmind.getTitle().length() + 1), builders.length(),"");
                    flag = false;
                }
            }
        }

        if (sb.toString().length() > 0){
            steps.add(step);
            step.setStep(xmind.getTitle());
            step.setExpectedResult(sb.toString());
            try {
                step.setTitle(builders.substring(0, builders.length()-1));
            }catch (Exception e){
                throw new RuntimeException("请检查每条记录的层级是否足够，每条记录至少要保证三个节点");
            }

        }else {
            // 值得比较的内容-优化：只对用例标题内容进行比较
            if (builders.length() > xmind.getTitle().length() && xmind.getTitle().equals(builders.substring(builders.length() - (xmind.getTitle().length() + 1), builders.length()-1))){
                builders.replace(builders.length() - (xmind.getTitle().length() + 1), builders.length(),"");
            }
        }

        return steps;
    }


    /**
     * @param xmind XMind 对象
     * @param steps XMind 步骤集合
     * @return 将XMind转为以步骤为基准的记录
     * */
    @Deprecated
    private static List<XMindStep> convertToBaseAction(XMind xmind, List<XMindStep> steps){
        List<XMind> xmindList = xmind.getChildren().getAttached();
        XMindStep step = new XMindStep();
        StringBuilder sb = new StringBuilder();
        for (XMind var: xmindList){
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
    @Deprecated
    private static List<XMindHead> convertToCaseTitleTest(XMind xmind, List<XMindHead> titles, StringBuilder builders){
        List<XMind> xmindList = xmind.getChildren().getAttached();
        XMindHead head = new XMindHead();
        builders.append(xmind.getTitle()).append("-");
        boolean flag = true;
        // 每个循环是一层
        for (XMind x: xmindList){
            if (x.getChildren() != null){
                convertToCaseTitleTest(x, titles, builders);
            }else {
                if (flag){
                    // 用例处理完了
                    builders.replace(builders.length() - (xmind.getTitle().length() + 1), builders.length(),"");
                    flag = false;
                }
            }
        }

        head.setTitle(builders.substring(0, builders.length()-1));
        titles.add(head);
        // 值得比较的内容
        if (builders.length() > xmind.getTitle().length() && xmind.getTitle().equals(builders.substring(builders.length() - (xmind.getTitle().length() + 1), builders.length()-1))){
            builders.replace(builders.length() - (xmind.getTitle().length() + 1), builders.length(),"");
        }
        return titles;
    }

    /**
     * 尝试优化步骤的获取功能
     * */
    @Deprecated
    private static List<XMindStep> convertToBaseActionTest(XMind xmind, List<XMindStep> steps, StringBuilder builders){
        List<XMind> xmindList = xmind.getChildren().getAttached();
        XMindStep step = new XMindStep();
        builders.append(xmind.getTitle()).append("-");
        StringBuilder sb = new StringBuilder();

        boolean flag = true;
        for (XMind var: xmindList){
            if (var.getChildren() != null){
                convertToBaseActionTest(var, steps, builders);
            }else {
                sb.append(var.getTitle());
                if (flag){
                    builders.replace(builders.length() - (xmind.getTitle().length() + 1), builders.length(),"");
                    flag = false;
                }
            }
        }

        steps.add(step);
        if (sb.toString().length() > 0){
            step.setStep(xmind.getTitle());
            step.setExpectedResult(sb.toString());
        }else {
            step.setTitle(builders.substring(0, builders.length()-1));
        }

        // 值得比较的内容
        if (builders.length() > xmind.getTitle().length() && xmind.getTitle().equals(builders.substring(builders.length() - (xmind.getTitle().length() + 1), builders.length()-1))){
            builders.replace(builders.length() - (xmind.getTitle().length() + 1), builders.length(),"");
        }
        return steps;
    }


    /**
     * 优化获取用例title
     * */
    /**
     public static  <R, P> void analysisXmindTOResultTest(XMindFile xmindFile){
     JSONArray array = JSON.parseArray(xmindFile.getBody());
     List<XMindStep> resultSteps = new ArrayList<>();
     for (Object o: array){
     XMindFrame<XMind, R, P> xmindFrame = JSON.parseObject(JSON.toJSONString(o), new TypeReference<XMindFrame<XMind, R, P>>(){});
     XMind xmind = xmindFrame.getRootTopic();
     String topicTitle = xmind.getTitle();
     // 尝试优化基础步骤
     List<XMindStep> stepList = convertToBaseAction(xmind,new ArrayList<>(), new StringBuilder());
     List<XMindStep> xmindCanvasSteps = handleXmindSteps(stepList, topicTitle);
     resultSteps.addAll(xmindCanvasSteps);
     Map<String, List<XMindStep>> mapSteps = stepList.stream().collect(Collectors.groupingBy(XMindStep::getTitle));
     // 优化用例标题的获取方式ing
     List<XMindHead> OptimizeTesting = convertToCaseTitleTest(xmind, new ArrayList<>(), new StringBuilder());
     Map<String, List<XMindHead>> MapOptimizeTesting = OptimizeTesting.stream().collect(Collectors.groupingBy(XMindHead::getTitle));
     List<XMindHead> lh = new ArrayList<>();
     for(List<XMindHead> s : MapOptimizeTesting.values()){
     lh.add(s.get(0));
     }
     lh = lh.stream().sorted(Comparator.comparing(XMindHead::getTitle)).collect(Collectors.toList());
     List<XMindHead> finalLh = lh;
     lh = lh.stream().filter(x->{
     for (XMindHead head: finalLh){
     if (head.getTitle().contains(x.getTitle()) && !head.getTitle().equals(x.getTitle())){
     return false;
     }
     }
     return true;
     }).collect(Collectors.toList());
     lh = lh.stream().sorted(Comparator.comparing(XMindHead::getTitle)).collect(Collectors.toList());
     System.out.println(lh);
     }
     }
     */

}
