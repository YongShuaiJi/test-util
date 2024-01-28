package org.xmind.transform.execute.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.stereotype.Component;
import org.xmind.strategy.CustomColumnCellStyleStrategy;
import org.xmind.strategy.CustomColumnWidthStyleStrategy;
import org.xmind.strategy.ExcelMergeStrategy;
import org.xmind.transform.dto.Export;
import org.xmind.transform.dto.XMindFile;
import org.xmind.transform.dto.XMindStep;
import org.xmind.transform.execute.XMindExportStrategy;

import java.util.List;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-01-28 11:13:41
 */
@Component
public class XMindToExcelExport extends XMindBaseData implements XMindExportStrategy<XMindFile> {
    @Override
    public void execute(XMindFile xmindFile) {
        Export export = getExportData(xmindFile, this);
        toExcelFile(export.getResultSteps(), export.getFileName());
    }

    /**
     * 将内容写入文件
     * @param steps 数据源
     * @param fileName 文件名称
     * */
    private void toExcelFile(List<XMindStep> steps, String fileName){
        // 从第二行后开始合并
        int mergeRowIndex = 1;
        // 需要合并的列
        int[] mergeColumnIndex = {0, 1, 2, 5};
        // 单元格样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(contentWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(fileName)
                .head(XMindStep.class)
                .sheet("测试用例")
                .registerWriteHandler(horizontalCellStyleStrategy) // 横向样式策略
                .registerWriteHandler(new CustomColumnCellStyleStrategy()) // 纵向样式策略
                .registerWriteHandler(new CustomColumnWidthStyleStrategy()) // 列宽策略
                .registerWriteHandler(new ExcelMergeStrategy(mergeRowIndex, mergeColumnIndex))
                .doWrite(steps);
    }
}
