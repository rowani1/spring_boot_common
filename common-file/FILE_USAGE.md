# common-file 使用示例

`common-file` 统一抽象了 `StorageService`，支持 `local/oss/minio`。

## 1. 配置（本地存储）

```yaml
common:
  file:
    storage-type: local
    local:
      upload-path: D:/data/upload
      url-prefix: /files
```

## 2. 配置（OSS）

```yaml
common:
  file:
    storage-type: oss
    oss:
      endpoint: https://oss-cn-hangzhou.aliyuncs.com
      access-key-id: xxx
      access-key-secret: xxx
      bucket-name: your-bucket
```

## 3. 配置（MinIO）

```yaml
common:
  file:
    storage-type: minio
    minio:
      endpoint: http://127.0.0.1:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: app-files
```

## 4. 业务中使用 StorageService

```java
@Resource
private StorageService storageService;

public String upload(MultipartFile file) throws IOException {
    String path = "user/2026/02/06/" + file.getOriginalFilename();
    try (InputStream is = file.getInputStream()) {
        return storageService.upload(is, path);
    }
}

public void delete(String path) {
    storageService.delete(path);
}

public String previewUrl(String path) {
    return storageService.getUrl(path);
}
```

说明：

- 本地存储会做路径越权校验，防止 `../` 逃逸。
- OSS/MinIO `getUrl` 默认返回临时签名 URL（当前实现约 1 小时）。
