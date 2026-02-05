package com.common.security.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 摘要算法工具类
 */
public final class DigestUtils {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private DigestUtils() {}

    /**
     * MD5摘要
     * 警告：MD5已不安全，仅供校验文件完整性等非安全场景使用
     */
    public static String md5(String input) {
        return digest(input, "MD5");
    }

    /**
     * SHA-1摘要
     * 警告：SHA-1已不安全，仅供兼容旧系统使用
     */
    public static String sha1(String input) {
        return digest(input, "SHA-1");
    }

    /**
     * SHA-256摘要
     */
    public static String sha256(String input) {
        return digest(input, "SHA-256");
    }

    /**
     * SHA-512摘要
     */
    public static String sha512(String input) {
        return digest(input, "SHA-512");
    }

    /**
     * 带盐值的SHA-256摘要
     * 注意：此方法适用于一般数据签名，不适用于密码存储
     * 密码存储建议使用 BCrypt/SCrypt/Argon2
     */
    public static String sha256WithSalt(String input, String salt) {
        if (input == null) {
            return null;
        }
        String salted = salt != null ? input + salt : input;
        return sha256(salted);
    }

    /**
     * 通用摘要计算
     */
    public static String digest(String input, String algorithm) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new CryptoException("计算" + algorithm + "摘要失败", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
