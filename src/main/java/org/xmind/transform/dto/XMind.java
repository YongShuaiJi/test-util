package org.xmind.transform.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
@Getter
@Setter
@ToString
public class XMind {
    private String id;
    private String clazz;
    private String title;
    public String getTitle(){
        return Optional.ofNullable(this.title).orElse("").trim();
    }
    private String structureClass;
    private TreeNode children;
    private BigDecimal customWidth;
    private String branch;
}
