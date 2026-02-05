package com.common.autoconfigure;

import com.common.security.config.SecurityProperties;
import com.common.security.crypto.AesUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 安全模块自动配置
 */
@AutoConfiguration
@ConditionalOnClass(AesUtils.class)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {
    // 工具类为静态方法，无需注册Bean
    // 配置属性通过@EnableConfigurationProperties自动注册
}
