package org.xmind.transform.dto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * */
@Getter
@Setter
@Component
public class XMindFile {
    private String name;
    private String body;
}

