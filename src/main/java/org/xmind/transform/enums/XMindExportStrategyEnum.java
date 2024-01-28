package org.xmind.transform.enums;

import org.xmind.transform.execute.XMindExportStrategy;
import org.xmind.transform.execute.impl.XMindToCSVExport;
import org.xmind.transform.execute.impl.XMindToExcelExport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-01-28 12:17:04
 */
public enum XMindExportStrategyEnum {
    csv(0, "csv", new XMindToCSVExport()),
    excel(1, "xlsx", new XMindToExcelExport());

    private Integer index;
    private String suffix;
    private XMindExportStrategy strategy;

    public Integer getIndex() {
        return index;
    }

    public String getSuffix() {
        return suffix;
    }

    public XMindExportStrategy getStrategy() {
        return strategy;
    }

    XMindExportStrategyEnum(Integer index, String suffix, XMindExportStrategy strategy) {
        this.index = index;
        this.suffix = suffix;
        this.strategy = strategy;
    }

    public static String getSuffix(XMindExportStrategy strategy){
        for (XMindExportStrategyEnum strategyEnum: XMindExportStrategyEnum.values()){
            if (strategyEnum.getStrategy().getClass().equals(strategy.getClass())){
                return strategyEnum.getSuffix();
            }
        }
        return null;
    }

    public static List<XMindExportStrategy> getAllXMindExportStrategy(){
        List<XMindExportStrategy> strategyList = new ArrayList<>();
        for (XMindExportStrategyEnum strategyEnum: XMindExportStrategyEnum.values()){
            strategyList.add(strategyEnum.getStrategy());
        }
        return strategyList;

    }
}
