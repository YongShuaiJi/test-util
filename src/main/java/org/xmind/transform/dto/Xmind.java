package org.xmind.transform.dto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.xmind.transform.dto.TreeNode;

import java.math.BigDecimal;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
@Getter
@Setter
@ToString
public class Xmind {
    private String id;
    private String clazz;
    private String title;
    private String structureClass;
    private TreeNode children;
    private BigDecimal customWidth;
    private String branch;
}
