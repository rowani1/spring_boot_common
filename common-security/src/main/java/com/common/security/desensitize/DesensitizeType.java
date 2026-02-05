package com.common.security.desensitize;

/**
 * 脱敏类型枚举
 */
public enum DesensitizeType {

    /**
     * 手机号：138****8888
     */
    MOBILE,

    /**
     * 身份证：110***********1234
     */
    ID_CARD,

    /**
     * 银行卡：6222 **** **** 1234
     */
    BANK_CARD,

    /**
     * 邮箱：a**@example.com
     */
    EMAIL,

    /**
     * 姓名：张*
     */
    NAME,

    /**
     * 地址：北京市朝阳区****
     */
    ADDRESS,

    /**
     * 密码：全部替换为 ******
     */
    PASSWORD,

    /**
     * 自定义：使用自定义策略
     */
    CUSTOM
}
