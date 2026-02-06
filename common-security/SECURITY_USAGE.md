# common-security 使用示例

本文档演示 `common-security` 模块的典型用法。

源码参考：

- `src/main/java/com/common/security/config/SecurityProperties.java`
- `src/main/java/com/common/security/crypto/AesUtils.java`
- `src/main/java/com/common/security/crypto/RsaUtils.java`
- `src/main/java/com/common/security/crypto/DigestUtils.java`
- `src/main/java/com/common/security/desensitize/Desensitize.java`

## 1. 配置项（可选）

如果项目引入了 `common-autoconfigure`（或 `common-starter`），`SecurityProperties` 会自动绑定：

```yaml
common:
  security:
    aes-key: your-base64-aes-key
    aes-iv: your-base64-aes-iv
    rsa-public-key: your-base64-rsa-public-key
    rsa-private-key: your-base64-rsa-private-key
    desensitize-enabled: true
```

说明：

- `aes-key` 需要是 Base64 编码后的 16/24/32 字节密钥。
- `aes-iv` 需要是 Base64 编码后的 16 字节向量（CBC 模式）。

## 2. AES 加解密

### 2.1 生成密钥和向量

```java
String base64Key = AesUtils.generateKey();
String base64Iv = AesUtils.generateIv();
```

### 2.2 CBC 模式（推荐）

```java
String plainText = "hello-security";
String cipherText = AesUtils.encryptCbc(plainText, base64Key, base64Iv);
String decrypted = AesUtils.decryptCbc(cipherText, base64Key, base64Iv);
```

### 2.3 ECB 模式（仅兼容旧系统）

```java
String cipherText = AesUtils.encryptEcb("hello", base64Key);
String plainText = AesUtils.decryptEcb(cipherText, base64Key);
```

## 3. RSA 加解密与签名

### 3.1 生成密钥对

```java
RsaUtils.KeyPairResult keyPair = RsaUtils.generateKeyPair();
String publicKey = keyPair.getPublicKey();
String privateKey = keyPair.getPrivateKey();
```

### 3.2 公钥加密 / 私钥解密

```java
String cipherText = RsaUtils.encrypt("order-123", publicKey);
String plainText = RsaUtils.decrypt(cipherText, privateKey);
```

### 3.3 签名与验签

```java
String data = "id=1001&amount=99.00";
String sign = RsaUtils.sign(data, privateKey);
boolean verified = RsaUtils.verify(data, sign, publicKey);
```

注意：当前实现使用 OAEP(SHA-256) 加密，2048 位密钥下单次加密明文长度上限约 190 字节，长文本建议使用“AES + RSA 混合加密”。

## 4. 摘要算法

```java
String md5 = DigestUtils.md5("abc");
String sha256 = DigestUtils.sha256("abc");
String sha512 = DigestUtils.sha512("abc");
String sha256WithSalt = DigestUtils.sha256WithSalt("abc", "salt-001");
```

说明：

- `MD5`、`SHA-1` 不适合安全敏感场景。
- 密码存储建议使用 `BCrypt/SCrypt/Argon2`，不要直接使用通用摘要。

## 5. 数据脱敏（JSON 序列化）

在返回对象字段上标注 `@Desensitize`：

```java
import com.common.security.desensitize.Desensitize;
import com.common.security.desensitize.DesensitizeType;
import lombok.Data;

@Data
public class UserVO {

    @Desensitize(type = DesensitizeType.NAME)
    private String realName;

    @Desensitize(type = DesensitizeType.MOBILE)
    private String mobile;

    @Desensitize(type = DesensitizeType.EMAIL)
    private String email;

    @Desensitize(type = DesensitizeType.CUSTOM, prefixLen = 2, suffixLen = 2, maskChar = '#')
    private String certNo;
}
```

Controller 返回时，Jackson 序列化会自动输出脱敏值。

## 6. 异常处理建议

加解密和摘要异常会抛出 `CryptoException`，业务层可统一转换：

```java
try {
    String encrypted = AesUtils.encryptCbc(content, key, iv);
} catch (CryptoException ex) {
    throw new BizException(500, "数据加密失败");
}
```
