package com.common.file.storage;

import com.common.core.exception.BizException;
import com.common.file.config.FileProperties.LocalProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现
 */
@Slf4j
public class LocalStorageService implements StorageService {

    private final LocalProperties properties;
    private final Path rootPath;

    public LocalStorageService(LocalProperties properties) {
        this.properties = properties;
        this.rootPath = Paths.get(properties.getUploadPath()).toAbsolutePath().normalize();
        initDirectory();
    }

    private void initDirectory() {
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new BizException(500, "无法创建上传目录: " + rootPath);
        }
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        if (inputStream == null || path == null || path.trim().isEmpty()) {
            throw new BizException(400, "文件流或路径不能为空");
        }

        Path targetPath = resolveAndValidatePath(path);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("文件上传成功: {}", targetPath);
            return getUrl(path);
        } catch (IOException e) {
            log.error("文件上传失败: {}", path, e);
            throw new BizException(500, "文件上传失败");
        }
    }

    @Override
    public InputStream download(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new BizException(400, "文件路径不能为空");
        }

        Path filePath = resolveAndValidatePath(path);
        if (!Files.exists(filePath)) {
            throw new BizException(404, "文件不存在");
        }

        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("文件下载失败: {}", path, e);
            throw new BizException(500, "文件下载失败");
        }
    }

    @Override
    public void delete(String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        Path filePath = resolveAndValidatePath(path);
        try {
            Files.deleteIfExists(filePath);
            log.debug("文件删除成功: {}", filePath);
        } catch (IOException e) {
            log.error("文件删除失败: {}", path, e);
            throw new BizException(500, "文件删除失败");
        }
    }

    @Override
    public String getUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String urlPrefix = properties.getUrlPrefix();
        if (urlPrefix == null || urlPrefix.trim().isEmpty()) {
            urlPrefix = "/files";
        }
        if (!urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix + "/";
        }
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        return urlPrefix + normalizedPath;
    }

    @Override
    public boolean exists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        Path filePath = resolveAndValidatePath(path);
        return Files.exists(filePath);
    }

    private Path resolveAndValidatePath(String path) {
        Path resolved = rootPath.resolve(path).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new BizException(400, "非法文件路径");
        }
        return resolved;
    }
}
