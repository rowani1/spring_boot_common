package com.common.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.common.core.exception.BizException;
import com.common.file.config.FileProperties.OssProperties;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云OSS存储实现
 */
@Slf4j
public class OssStorageService implements StorageService {

    private final OssProperties properties;
    private final OSS ossClient;

    public OssStorageService(OssProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("OSS配置不能为空");
        }
        if (properties.getEndpoint() == null || properties.getEndpoint().trim().isEmpty()) {
            throw new IllegalArgumentException("OSS endpoint不能为空");
        }
        if (properties.getAccessKeyId() == null || properties.getAccessKeyId().trim().isEmpty()) {
            throw new IllegalArgumentException("OSS accessKeyId不能为空");
        }
        if (properties.getBucketName() == null || properties.getBucketName().trim().isEmpty()) {
            throw new IllegalArgumentException("OSS bucketName不能为空");
        }
        this.properties = properties;
        this.ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        if (inputStream == null || path == null || path.trim().isEmpty()) {
            throw new BizException(400, "文件流或路径不能为空");
        }

        String objectKey = normalizePath(path);
        try {
            ossClient.putObject(properties.getBucketName(), objectKey, inputStream);
            log.debug("OSS文件上传成功: {}", objectKey);
            return getUrl(objectKey);
        } catch (Exception e) {
            log.error("OSS文件上传失败: {}", path, e);
            throw new BizException(500, "文件上传失败");
        }
    }

    @Override
    public InputStream download(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new BizException(400, "文件路径不能为空");
        }

        String objectKey = normalizePath(path);
        try {
            OSSObject ossObject = ossClient.getObject(properties.getBucketName(), objectKey);
            return ossObject.getObjectContent();
        } catch (OSSException e) {
            if ("NoSuchKey".equals(e.getErrorCode())) {
                throw new BizException(404, "文件不存在");
            }
            log.error("OSS文件下载失败: {}", path, e);
            throw new BizException(500, "文件下载失败");
        } catch (Exception e) {
            log.error("OSS文件下载失败: {}", path, e);
            throw new BizException(500, "文件下载失败");
        }
    }

    @Override
    public void delete(String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        String objectKey = normalizePath(path);
        try {
            ossClient.deleteObject(properties.getBucketName(), objectKey);
            log.debug("OSS文件删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", path, e);
            throw new BizException(500, "文件删除失败");
        }
    }

    @Override
    public String getUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        String objectKey = normalizePath(path);
        // 生成1小时有效期的签名URL
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        URL url = ossClient.generatePresignedUrl(properties.getBucketName(), objectKey, expiration);
        return url.toString();
    }

    @Override
    public boolean exists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        String objectKey = normalizePath(path);
        try {
            return ossClient.doesObjectExist(properties.getBucketName(), objectKey);
        } catch (Exception e) {
            log.warn("OSS文件存在性检查失败: {}", path, e);
            return false;
        }
    }

    private String normalizePath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
