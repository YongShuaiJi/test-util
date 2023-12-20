package org.xmind.transform;

import org.apache.commons.io.IOUtils;
import org.xmind.transform.dto.XMindFile;
import org.xmind.transform.execute.XMindExportStrategy;
import org.xmind.transform.execute.XMindToCSVExport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @createDate 2023/3/22
 * */
public class XMindTransform {
    public static List<XMindFile> xmindJSONToStringList = new ArrayList<>();
    private static final String sourceFileName = "content.json"; // 标准的XMind内容文件
    private static Map<String, XMindExportStrategy> xmindExportStrategyMap = new HashMap<>();

    static {
        xmindExportStrategyMap.put("csv", new XMindToCSVExport());
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
            xmindExportStrategyMap.get("csv").execute(xmindFile);
        }

    }
}
