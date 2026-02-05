package com.common.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置属性
 */
@Data
@ConfigurationProperties(prefix = "common.file")
public class FileProperties {

    /**
     * 存储类型：local, oss, minio
     */
    private String storageType = "local";

    /**
     * 本地存储配置
     */
    private LocalProperties local = new LocalProperties();

    /**
     * 阿里云OSS配置
     */
    private OssProperties oss = new OssProperties();

    /**
     * MinIO配置
     */
    private MinioProperties minio = new MinioProperties();

    @Data
    public static class LocalProperties {
        /**
         * 上传文件存储路径
         */
        private String uploadPath = "/data/upload";

        /**
         * 文件访问URL前缀
         */
        private String urlPrefix = "/files";
    }

    @Data
    public static class OssProperties {
        /**
         * OSS服务端点
         */
        private String endpoint;

        /**
         * 访问密钥ID
         */
        private String accessKeyId;

        /**
         * 访问密钥Secret
         */
        private String accessKeySecret;

        /**
         * 存储桶名称
         */
        private String bucketName;
    }

    @Data
    public static class MinioProperties {
        /**
         * MinIO服务端点
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 密钥
         */
        private String secretKey;

        /**
         * 存储桶名称
         */
        private String bucketName;
    }
}
