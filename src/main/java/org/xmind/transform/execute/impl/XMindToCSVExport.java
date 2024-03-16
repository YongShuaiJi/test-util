package org.xmind.transform.execute.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.xmind.transform.dto.Export;
import org.xmind.transform.dto.XMindFile;
import org.xmind.transform.dto.XMindStep;
import org.xmind.transform.enums.XrayCase;
import org.xmind.transform.execute.XMindExportStrategy;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2023/3/22
 */
@Slf4j
@Component
public class XMindToCSVExport extends XMindBaseData implements XMindExportStrategy {

    @Override
    public void execute(XMindFile xmindFile) {
        Export exportData = getExportData(xmindFile, this);
        // 校验对象
        Validator validator;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        exportData.getResultSteps().forEach(xMindStep -> {
            Set<ConstraintViolation<XMindStep>> constraintViolations = validator.validate(xMindStep);
            Iterator<ConstraintViolation<XMindStep>> iterator = constraintViolations.iterator();
            while (iterator.hasNext()){
                ConstraintViolation<XMindStep> constraintViolation = iterator.next();
                throw new RuntimeException(constraintViolation.getMessage()
                        + "，" + "用例标题：" +
                        constraintViolation.getRootBean().getTitle());
            }
        });
        toFile(exportData.getResultSteps(), exportData.getFileName());
    }

    /**
     * 将内容写入文件
     * @param steps 数据源
     * @param fileName 文件名称
     * */
    private void toFile(List<XMindStep> steps, String fileName){
        String firstLine = XrayCase.getFormatNames() + System.lineSeparator();
        try {
            FileUtils.writeStringToFile(new File(fileName), firstLine, "UTF-8");
            FileUtils.writeLines(new File(fileName), steps,true);
        } catch (IOException e) {
            log.error("文件写入错误：{}", e.getMessage());
        }
    }

}
