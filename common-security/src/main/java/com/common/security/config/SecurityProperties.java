package com.common.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全配置属性
 */
@ConfigurationProperties(prefix = "common.security")
public class SecurityProperties {

    /**
     * AES密钥（16/24/32字节）
     */
    private String aesKey;

    /**
     * AES向量（16字节，CBC模式使用）
     */
    private String aesIv;

    /**
     * RSA公钥
     */
    private String rsaPublicKey;

    /**
     * RSA私钥
     */
    private String rsaPrivateKey;

    /**
     * 脱敏开关
     */
    private boolean desensitizeEnabled = true;

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getAesIv() {
        return aesIv;
    }

    public void setAesIv(String aesIv) {
        this.aesIv = aesIv;
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public boolean isDesensitizeEnabled() {
        return desensitizeEnabled;
    }

    public void setDesensitizeEnabled(boolean desensitizeEnabled) {
        this.desensitizeEnabled = desensitizeEnabled;
    }
}
