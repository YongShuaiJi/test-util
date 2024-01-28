package org.xmind.strategy;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.AbstractVerticalCellStyleStrategy;
import org.apache.poi.ss.usermodel.HorizontalAlignment;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-01-28 14:08:10
 */
public class CustomColumnCellStyleStrategy extends AbstractVerticalCellStyleStrategy {

    /**
     * Returns the column width corresponding to each column head.
     *
     * @param context
     * @return
     */
    @Override
    protected WriteCellStyle contentCellStyle(CellWriteHandlerContext context) {
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 是否自动换行
        contentWriteCellStyle.setWrapped(true);
        if (context.getColumnIndex() == 0 || context.getColumnIndex() == 2){
            // 水平对齐类型
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            // 自动收缩 ToFit
            contentWriteCellStyle.setShrinkToFit(true);
            return contentWriteCellStyle;
        }
        return contentWriteCellStyle;
    }

}
