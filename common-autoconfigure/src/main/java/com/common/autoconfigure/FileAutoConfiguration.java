package com.common.autoconfigure;

import com.common.file.config.FileProperties;
import com.common.file.storage.LocalStorageService;
import com.common.file.storage.MinioStorageService;
import com.common.file.storage.OssStorageService;
import com.common.file.storage.StorageService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 文件存储自动配置
 */
@AutoConfiguration
@ConditionalOnClass(StorageService.class)
@EnableConfigurationProperties(FileProperties.class)
public class FileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "common.file.storage-type", havingValue = "local", matchIfMissing = true)
    public StorageService localStorageService(FileProperties properties) {
        return new LocalStorageService(properties.getLocal());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "common.file.storage-type", havingValue = "oss")
    @ConditionalOnClass(name = "com.aliyun.oss.OSS")
    public StorageService ossStorageService(FileProperties properties) {
        return new OssStorageService(properties.getOss());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "common.file.storage-type", havingValue = "minio")
    @ConditionalOnClass(name = "io.minio.MinioClient")
    public StorageService minioStorageService(FileProperties properties) {
        return new MinioStorageService(properties.getMinio());
    }
}
