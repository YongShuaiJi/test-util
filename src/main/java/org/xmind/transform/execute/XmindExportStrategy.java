package org.xmind.transform.execute;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2023/3/22
 */
@FunctionalInterface
public interface XmindExportStrategy<E> {

    void execute(E e);
}
