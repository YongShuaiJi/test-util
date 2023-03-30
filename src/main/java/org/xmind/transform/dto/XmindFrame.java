package org.xmind.transform.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
@Getter
@Setter
@ToString
public class XmindFrame<T, R, P> {
    private String id;
    private String clazz;
    private String title;
    private T rootTopic;
    private List<R> extensions;
    private P theme;
    private String topicPositioning;
    private String coreVersion;
}
