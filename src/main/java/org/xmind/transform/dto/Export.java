package org.xmind.transform.dto;

import lombok.Data;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-01-28 12:05:41
 */
@Data
@Component
public class Export {
    String fileName;
    List<XMindStep> resultSteps;
}
