package org.xmind.transform.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;

/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-01-27 14:42:51
 */
@Configuration
@ComponentScan(value = "org.xmind.transform",
        includeFilters = {@Filter(type = FilterType.ANNOTATION, value = Component.class)}
)
public class ObjectConfig {
}
