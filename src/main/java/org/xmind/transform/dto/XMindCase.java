package org.xmind.transform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
@Getter
@Setter
public class XMindCase {
    private String caseTitle;
    private List<XMindStep> action;
}
