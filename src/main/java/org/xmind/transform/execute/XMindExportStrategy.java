package org.xmind.transform.execute;

import org.xmind.transform.dto.XMindFile;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2023/3/22
 */
@FunctionalInterface
public interface XMindExportStrategy {
    void execute(XMindFile xMindFile);
}
