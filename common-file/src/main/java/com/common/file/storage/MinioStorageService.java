package com.common.file.storage;

import com.common.core.exception.BizException;
import com.common.file.config.FileProperties.MinioProperties;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO存储实现
 */
@Slf4j
public class MinioStorageService implements StorageService {

    private final MinioProperties properties;
    private final MinioClient minioClient;

    public MinioStorageService(MinioProperties properties) {
        this.properties = properties;
        this.minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucketName()).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getBucketName()).build()
                );
                log.info("MinIO存储桶创建成功: {}", properties.getBucketName());
            }
        } catch (Exception e) {
            log.error("MinIO存储桶检查/创建失败", e);
            throw new BizException(500, "MinIO存储桶初始化失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        if (inputStream == null || path == null || path.trim().isEmpty()) {
            throw new BizException(400, "文件流或路径不能为空");
        }

        String objectName = normalizePath(path);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, -1, 10485760) // 10MB part size
                            .build()
            );
            log.debug("MinIO文件上传成功: {}", objectName);
            return getUrl(objectName);
        } catch (Exception e) {
            log.error("MinIO文件上传失败: {}", path, e);
            throw new BizException(500, "文件上传失败");
        }
    }

    @Override
    public InputStream download(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new BizException(400, "文件路径不能为空");
        }

        String objectName = normalizePath(path);
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new BizException(404, "文件不存在");
            }
            log.error("MinIO文件下载失败: {}", path, e);
            throw new BizException(500, "文件下载失败");
        } catch (Exception e) {
            log.error("MinIO文件下载失败: {}", path, e);
            throw new BizException(500, "文件下载失败");
        }
    }

    @Override
    public void delete(String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        String objectName = normalizePath(path);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.debug("MinIO文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("MinIO文件删除失败: {}", path, e);
            throw new BizException(500, "文件删除失败");
        }
    }

    @Override
    public String getUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        String objectName = normalizePath(path);
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO获取URL失败: {}", path, e);
            throw new BizException(500, "获取文件URL失败");
        }
    }

    @Override
    public boolean exists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        String objectName = normalizePath(path);
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizePath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
