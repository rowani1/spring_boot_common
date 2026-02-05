package com.common.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 公共模块自动配置入口
 * 各子配置类通过 AutoConfiguration.imports 单独注册，避免强耦合
 */
@AutoConfiguration
public class CommonAutoConfiguration {
}
