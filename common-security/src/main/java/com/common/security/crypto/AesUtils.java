package com.common.security.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加解密工具类
 */
public final class AesUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION_ECB = "AES/ECB/PKCS5Padding";
    private static final String TRANSFORMATION_CBC = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128;

    private AesUtils() {}

    /**
     * 生成AES密钥（Base64编码）
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new CryptoException("生成AES密钥失败", e);
        }
    }

    /**
     * 生成16字节IV向量（Base64编码）
     */
    public static String generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    /**
     * AES-ECB加密
     * 警告：ECB模式不隐藏数据模式，仅适用于兼容旧系统或加密随机数据
     * 敏感数据建议使用 AES-CBC 或 AES-GCM
     */
    public static String encryptEcb(String plainText, String base64Key) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            SecretKeySpec keySpec = createKeySpec(base64Key);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_ECB);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CryptoException("AES-ECB加密失败", e);
        }
    }

    /**
     * AES-ECB解密
     */
    public static String decryptEcb(String cipherText, String base64Key) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            SecretKeySpec keySpec = createKeySpec(base64Key);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_ECB);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("AES-ECB解密失败", e);
        }
    }

    /**
     * AES-CBC加密
     */
    public static String encryptCbc(String plainText, String base64Key, String base64Iv) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            SecretKeySpec keySpec = createKeySpec(base64Key);
            IvParameterSpec ivSpec = createIvSpec(base64Iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CryptoException("AES-CBC加密失败", e);
        }
    }

    /**
     * AES-CBC解密
     */
    public static String decryptCbc(String cipherText, String base64Key, String base64Iv) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            SecretKeySpec keySpec = createKeySpec(base64Key);
            IvParameterSpec ivSpec = createIvSpec(base64Iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("AES-CBC解密失败", e);
        }
    }

    private static SecretKeySpec createKeySpec(String base64Key) {
        if (base64Key == null || base64Key.isEmpty()) {
            throw new CryptoException("AES密钥不能为空");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new CryptoException("AES密钥长度必须是16/24/32字节");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static IvParameterSpec createIvSpec(String base64Iv) {
        if (base64Iv == null || base64Iv.isEmpty()) {
            throw new CryptoException("IV向量不能为空");
        }
        byte[] ivBytes = Base64.getDecoder().decode(base64Iv);
        if (ivBytes.length != 16) {
            throw new CryptoException("IV向量长度必须是16字节");
        }
        return new IvParameterSpec(ivBytes);
    }
}
