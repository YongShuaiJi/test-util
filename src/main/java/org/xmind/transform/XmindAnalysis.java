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
public class XmindAnalysis {

    public static List<XmindFile> xmindJSONToStringList = new ArrayList<>();

    public static String smokingFlag = "冒烟";

    private static String xpathSeparator = "/"; // 目录之间连接符

//    private static final int hierarchy = 2; // 默认固定两层,需要时调整， hierarchy 为主题之下几层子主题，对应主目录下几层子目录

    private static String sourceFileName = "content.json";


    static {
        // (*^▽^*)
        File[] xmindFileSourceList = new File("./xfiles/source").listFiles((dir, name) -> name.endsWith(".xmind"));
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
        for (XmindFile xmindFile : xmindJSONToStringList){
            analysisXmindTOResult(xmindFile);
        }
    }

    /**
     * 解析XMind内容文件，将内容文件转化为格式化的记录
     * @param xmindFile XMind 文件对象
     * */
    public static <R, P> void analysisXmindTOResult(XmindFile xmindFile){
        List<JSONObject> array = JSON.parseArray(xmindFile.getBody(), JSONObject.class);
        List<XmindStep> resultSteps = new ArrayList<>();
        for (JSONObject o: array){
            XmindFrame<Xmind, R, P> xmindFrame = JSON.parseObject(o.toString(), new TypeReference<XmindFrame<Xmind, R, P>>(){});
            Xmind xmind = xmindFrame.getRootTopic();
            String topicTitle = xmind.getTitle();
            List<XmindStep> stepList = convertToBaseAction(xmind,new ArrayList<>(), new StringBuilder());
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
            List<XmindStep> xmindCanvasSteps = handleXmindSteps(stepList, topicTitle, hierarchy);
            resultSteps.addAll(xmindCanvasSteps);
        }
        String fileName = "./xfiles/target/" + xmindFile.getName();
        toFile(resultSteps, fileName);
    }

    /**
     * 最初的处理方式，用于处理 convertToBaseAction(Xmind xmind, List<XmindStep> steps) 方法计算出来的数据集合
     * 当前有了更好的计算方式可以把基础数据集合算的更加准确。
     * */
    @Deprecated
    private static List<XmindStep> handleSteps(List<XmindStep> stepList, String topicTitle, int hierarchy){

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
        Map<String, List<XmindStep>> tmepCase = sourceXmindStep.stream().collect(Collectors.groupingBy(XmindStep::getTitle));
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

    /**
     * 基础步骤的格式化 给需要的属性赋值
     * @param stepList 基础数据源
     * @param topicTitle 画布主题
     * @return  格式化基准步骤
     * */
    private static List<XmindStep> handleXmindSteps(List<XmindStep> stepList, String topicTitle, int hierarchy){
        Map<String, List<XmindStep>> mapStep = stepList.stream().collect(Collectors.groupingBy(XmindStep::getTitle));
        TreeMap stepSortMap = new TreeMap(mapStep);
        Iterator<Map.Entry<String, List<XmindStep>>> stepIterator = stepSortMap.entrySet().iterator();
        // setCaseId
        int tmepNum = 0;
        while (stepIterator.hasNext()){
            tmepNum+=1;
            Map.Entry<String, List<XmindStep>> next = stepIterator.next();
            for (XmindStep xmindStep : next.getValue()){
                xmindStep.setCaseId(tmepNum);
            }
        }
        // 格式化用例步骤 提供业务需要的用例格式
        List<XmindStep> formatSteps = new ArrayList<>();
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
        List<XmindStep> resultSteps = formatSteps.stream().sorted(Comparator.comparing(XmindStep::getCaseId)).collect(Collectors.toList());
        return resultSteps;
    }

    /**
     * 将内容写入文件
     * @param steps 数据源
     * @param fileName 文件名称
     * */
    private static void toFile(List<XmindStep> steps, String fileName){
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
     * 进一步优化，获取步骤后先获取title再循环：
     * @param xmind Xmind 起始对象程序解析的数据源
     * @param steps Xmind 步骤集合，程序存储的结果极氪
     * @param builders 用于步骤间标题的传递
     * @return 将XMind转为以步骤为基准的记录
     * */
    private static List<XmindStep> convertToBaseAction(Xmind xmind, List<XmindStep> steps, StringBuilder builders){
        List<Xmind> xmindList = xmind.getChildren().getAttached();
        XmindStep step = new XmindStep();
        builders.append(xmind.getTitle()).append("-");
        StringBuilder sb = new StringBuilder();

        boolean flag = true;
        for (Xmind var: xmindList){
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
     * @param xmind Xmind 对象
     * @param steps Xmind 步骤集合
     * @return 将XMind转为以步骤为基准的记录
     * */
    @Deprecated
    private static List<XmindStep> convertToBaseAction(Xmind xmind, List<XmindStep> steps){
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
    @Deprecated
    private static List<XmindHead> convertToCaseTitleTest(Xmind xmind, List<XmindHead> titles, StringBuilder builders){
        List<Xmind> xmindList = xmind.getChildren().getAttached();
        XmindHead head = new XmindHead();
        builders.append(xmind.getTitle()).append("-");
        boolean flag = true;
        // 每个循环是一层
        for (Xmind x: xmindList){
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
    private static List<XmindStep> convertToBaseActionTest(Xmind xmind, List<XmindStep> steps, StringBuilder builders){
        List<Xmind> xmindList = xmind.getChildren().getAttached();
        XmindStep step = new XmindStep();
        builders.append(xmind.getTitle()).append("-");
        StringBuilder sb = new StringBuilder();

        boolean flag = true;
        for (Xmind var: xmindList){
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
     public static  <R, P> void analysisXmindTOResultTest(XmindFile xmindFile){
     JSONArray array = JSON.parseArray(xmindFile.getBody());
     List<XmindStep> resultSteps = new ArrayList<>();
     for (Object o: array){
     XmindFrame<Xmind, R, P> xmindFrame = JSON.parseObject(JSON.toJSONString(o), new TypeReference<XmindFrame<Xmind, R, P>>(){});
     Xmind xmind = xmindFrame.getRootTopic();
     String topicTitle = xmind.getTitle();
     // 尝试优化基础步骤
     List<XmindStep> stepList = convertToBaseAction(xmind,new ArrayList<>(), new StringBuilder());
     List<XmindStep> xmindCanvasSteps = handleXmindSteps(stepList, topicTitle);
     resultSteps.addAll(xmindCanvasSteps);
     Map<String, List<XmindStep>> mapSteps = stepList.stream().collect(Collectors.groupingBy(XmindStep::getTitle));
     // 优化用例标题的获取方式ing
     List<XmindHead> OptimizeTesting = convertToCaseTitleTest(xmind, new ArrayList<>(), new StringBuilder());
     Map<String, List<XmindHead>> MapOptimizeTesting = OptimizeTesting.stream().collect(Collectors.groupingBy(XmindHead::getTitle));
     List<XmindHead> lh = new ArrayList<>();
     for(List<XmindHead> s : MapOptimizeTesting.values()){
     lh.add(s.get(0));
     }
     lh = lh.stream().sorted(Comparator.comparing(XmindHead::getTitle)).collect(Collectors.toList());
     List<XmindHead> finalLh = lh;
     lh = lh.stream().filter(x->{
     for (XmindHead head: finalLh){
     if (head.getTitle().contains(x.getTitle()) && !head.getTitle().equals(x.getTitle())){
     return false;
     }
     }
     return true;
     }).collect(Collectors.toList());
     lh = lh.stream().sorted(Comparator.comparing(XmindHead::getTitle)).collect(Collectors.toList());
     System.out.println(lh);
     }
     }
     */

}
