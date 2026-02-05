package com.common.security.desensitize;

/**
 * 脱敏策略工具类
 */
public final class DesensitizeStrategy {

    private DesensitizeStrategy() {}

    /**
     * 手机号脱敏：138****8888
     */
    public static String mobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile;
        }
        return mask(mobile, 3, 4, '*');
    }

    /**
     * 身份证脱敏：110***********1234
     */
    public static String idCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return mask(idCard, 3, 4, '*');
    }

    /**
     * 银行卡脱敏：6222 **** **** 1234
     */
    public static String bankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        return mask(bankCard, 4, 4, '*');
    }

    /**
     * 邮箱脱敏：a**@example.com
     */
    public static String email(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        return mask(prefix, 1, 0, '*') + suffix;
    }

    /**
     * 姓名脱敏：张*、张*明
     */
    public static String name(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return mask(name, 1, 1, '*');
    }

    /**
     * 地址脱敏：北京市朝阳区****
     */
    public static String address(String address) {
        if (address == null || address.length() < 8) {
            return address;
        }
        int prefixLen = Math.min(6, address.length() / 2);
        return mask(address, prefixLen, 0, '*');
    }

    /**
     * 密码脱敏：全部替换为 ******
     */
    public static String password(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return "******";
    }

    /**
     * 自定义脱敏
     *
     * @param value     原始值
     * @param prefixLen 前缀保留长度
     * @param suffixLen 后缀保留长度
     * @param maskChar  替换字符
     */
    public static String custom(String value, int prefixLen, int suffixLen, char maskChar) {
        return mask(value, prefixLen, suffixLen, maskChar);
    }

    /**
     * 通用脱敏方法
     */
    private static String mask(String value, int prefixLen, int suffixLen, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int len = value.length();
        if (prefixLen < 0) prefixLen = 0;
        if (suffixLen < 0) suffixLen = 0;

        if (prefixLen + suffixLen >= len) {
            return value;
        }

        StringBuilder sb = new StringBuilder(len);
        sb.append(value, 0, prefixLen);
        for (int i = prefixLen; i < len - suffixLen; i++) {
            sb.append(maskChar);
        }
        if (suffixLen > 0) {
            sb.append(value.substring(len - suffixLen));
        }
        return sb.toString();
    }
}
