package com.common.file.storage;

import java.io.InputStream;

/**
 * 存储服务抽象接口
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param path        存储路径（含文件名）
     * @return 文件访问路径
     */
    String upload(InputStream inputStream, String path);

    /**
     * 下载文件
     *
     * @param path 文件路径
     * @return 文件输入流
     */
    InputStream download(String path);

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    void delete(String path);

    /**
     * 获取文件访问URL
     *
     * @param path 文件路径
     * @return 完整访问URL
     */
    String getUrl(String path);

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return true-存在，false-不存在
     */
    boolean exists(String path);
}
