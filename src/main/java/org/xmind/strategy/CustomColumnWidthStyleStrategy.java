package org.xmind.strategy;

import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.CellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-01-28 14:08:31
 */
public class CustomColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

    private final Map<Integer, Map<Integer, Integer>> CACHE = new HashMap<>();

    /**
     * Sets the column width when head create
     *
     * @param writeSheetHolder
     * @param cellDataList
     * @param cell
     * @param head
     * @param relativeRowIndex
     * @param isHead
     */
    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList,
                                  Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {

        boolean needSetWidth = isHead || !CollectionUtils.isEmpty(cellDataList);
        if (needSetWidth) {
            Map<Integer, Integer> maxColumnWidthMap = CACHE.computeIfAbsent(writeSheetHolder.getSheetNo(), k -> new HashMap<>());

            Integer columnWidth = this.dataLength(cellDataList, cell, isHead);
            // 单元格文本最长为60
            if (columnWidth >= 0) {
                if (columnWidth > 60) {
                    columnWidth = 60;
                }
                Integer maxColumnWidth = maxColumnWidthMap.get(cell.getColumnIndex());
                // 兼容最大单元格
                if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
                    maxColumnWidthMap.put(cell.getColumnIndex(), columnWidth);
                    Sheet sheet = writeSheetHolder.getSheet();
                    sheet.setColumnWidth(cell.getColumnIndex(), columnWidth * 256);
                }
            }
        }
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Integer dataLength(List<WriteCellData<?>> cellDataList, Cell cell, Boolean isHead) {
        if (isHead) {
            return cell.getStringCellValue().getBytes().length;
        } else {
            CellData<?> cellData = cellDataList.get(0);
            CellDataTypeEnum type = cellData.getType();
            if (type == null) {
                return -1;
            } else {
                switch (type) {
                    case STRING:
                        // 换行符
                        int index = cellData.getStringValue().indexOf("\n");
                        // 优化处理 \r\n 的场景
                        // 将最长的放到最上面
                        return index != -1 ?
                                Arrays.stream(cellData.getStringValue().split("\n"))
                                        .map(o -> o.trim()) // 优化处理 \r\n 的场景
                                        .max(Comparator.comparing(String::length)) // 将最长的放到最上面
                                        .get()
                                        .getBytes()
                                        .length
                                : cellData.getStringValue().getBytes().length;
                    case BOOLEAN:
                        return cellData.getBooleanValue().toString().getBytes().length;
                    case NUMBER:
                        return cellData.getNumberValue().toString().getBytes().length;
                    default:
                        return -1;
                }
            }
        }
    }
}
