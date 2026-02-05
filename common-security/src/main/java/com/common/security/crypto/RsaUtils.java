package com.common.security.crypto;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加解密工具类
 */
public final class RsaUtils {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;
    private static final int MAX_ENCRYPT_BLOCK = 190; // 2048位密钥 OAEP最大加密长度

    private RsaUtils() {}

    /**
     * 生成RSA密钥对
     */
    public static KeyPairResult generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(KEY_SIZE, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            return new KeyPairResult(publicKey, privateKey);
        } catch (Exception e) {
            throw new CryptoException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 公钥加密（使用OAEP填充，适用于短文本）
     * 注意：RSA加密有长度限制，2048位密钥最大加密190字节
     * 长文本建议使用混合加密（AES加密数据，RSA加密AES密钥）
     */
    public static String encrypt(String plainText, String base64PublicKey) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        if (data.length > MAX_ENCRYPT_BLOCK) {
            throw new CryptoException("RSA加密数据过长，最大" + MAX_ENCRYPT_BLOCK + "字节，当前" + data.length + "字节");
        }
        try {
            PublicKey publicKey = getPublicKey(base64PublicKey);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CryptoException("RSA加密失败", e);
        }
    }

    /**
     * 私钥解密（使用OAEP填充）
     */
    public static String decrypt(String cipherText, String base64PrivateKey) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            PrivateKey privateKey = getPrivateKey(base64PrivateKey);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("RSA解密失败", e);
        }
    }

    /**
     * 私钥签名
     */
    public static String sign(String data, String base64PrivateKey) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            PrivateKey privateKey = getPrivateKey(base64PrivateKey);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new CryptoException("RSA签名失败", e);
        }
    }

    /**
     * 公钥验签
     */
    public static boolean verify(String data, String sign, String base64PublicKey) {
        if (data == null || sign == null) {
            return false;
        }
        try {
            PublicKey publicKey = getPublicKey(base64PublicKey);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new CryptoException("RSA验签失败", e);
        }
    }

    private static PublicKey getPublicKey(String base64PublicKey) throws Exception {
        if (base64PublicKey == null || base64PublicKey.isEmpty()) {
            throw new CryptoException("RSA公钥不能为空");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    private static PrivateKey getPrivateKey(String base64PrivateKey) throws Exception {
        if (base64PrivateKey == null || base64PrivateKey.isEmpty()) {
            throw new CryptoException("RSA私钥不能为空");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 密钥对结果
     */
    public static class KeyPairResult {
        private final String publicKey;
        private final String privateKey;

        public KeyPairResult(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }
    }
}
