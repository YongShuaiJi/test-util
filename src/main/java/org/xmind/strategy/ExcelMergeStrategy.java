package org.xmind.strategy;


import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.csv.CsvCell;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-01-28 14:08:52
 */
public class ExcelMergeStrategy implements CellWriteHandler {

    // 从第几行开始合并
    private final int mergeRowIndex;

    // 合并列的范围，合并哪些列
    private final int[] mergeColumnIndex;

    public ExcelMergeStrategy(int mergeRowIndex, int[] mergeColumnIndex){
        this.mergeRowIndex = mergeRowIndex;
        this.mergeColumnIndex = mergeColumnIndex;
    }

    // 单元格上所有操作完成后执行此逻辑
    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        // 当前行
        int rowIndex = cell.getRowIndex();
        // 当前列
        int columnIndex = cell.getColumnIndex();

        // 行的开始逻辑，之前的行合并不考虑
        if(rowIndex > mergeRowIndex){
            // 遍历需要合并的列
            for (int index : mergeColumnIndex) {
                if (columnIndex == index) {
                    // 找到需要合并的列
                    mergeWithPrevRow(writeSheetHolder, cell, rowIndex, columnIndex);
                    break;
                }
            }
        }
    }

    // 当前单元格往上合并
    private void mergeWithPrevRow(WriteSheetHolder writeSheetHolder, Cell cell, int rowIndex, int columnIndex) {
        // 获取当前列的数据
        Object curData = cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.getNumericCellValue();
        // 获取上一列的数据
        if(cell instanceof CsvCell){
            throw new RuntimeException("导出为Excel中不能强制导出为CSV");
        }
        Cell preCell = cell.getSheet().getRow(rowIndex - 1).getCell(columnIndex);
        Object preData = preCell.getCellType()== CellType.STRING ? preCell.getStringCellValue() : preCell.getNumericCellValue();
        // 将当前单元格数据与上一个单元格数据比较
        Boolean dataBool = preData.equals(curData);

        // 按用例主键ID合并，不是一个ID的记录不合并
        Cell current_cell = cell.getRow().getCell(0);
        Object var1 = current_cell.getCellType() == CellType.STRING ? current_cell.getStringCellValue() : current_cell.getNumericCellValue();
        Cell pre_cell = cell.getSheet().getRow(rowIndex - 1).getCell(0);
        Object var2 = pre_cell.getCellType() == CellType.STRING ? pre_cell.getStringCellValue() : pre_cell.getNumericCellValue();
        Boolean bool = var1.equals(var2);
        // 相同则进行合并
        if (dataBool && bool) {
            Sheet sheet = writeSheetHolder.getSheet();
            List<CellRangeAddress> mergeRegions = sheet.getMergedRegions();
            boolean isMerged = false;
            for (int i = 0; i < mergeRegions.size() && !isMerged; i++) {
                CellRangeAddress cellRangeAddr = mergeRegions.get(i);
                // 若上一个单元格已经被合并，则先移出原有的合并单元，再重新添加合并单元
                if (cellRangeAddr.isInRange(rowIndex - 1, columnIndex)) {
                    sheet.removeMergedRegion(i);
                    cellRangeAddr.setLastRow(rowIndex);
                    sheet.addMergedRegion(cellRangeAddr);
                    isMerged = true;
                }
            }
            // 若上一个单元格未被合并，则新增合并单元
            if (!isMerged) {
                CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex - 1, rowIndex, columnIndex, columnIndex);
                sheet.addMergedRegion(cellRangeAddress);
            }
        }
    }
}

