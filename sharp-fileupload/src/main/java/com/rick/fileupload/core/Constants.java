package com.rick.fileupload.core;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 常量类
 * @author: Rick.Xu
 * @date: 10/19/18 14:26
 * @Copyright: 2018 www.yodean.com. All rights reserved.
 */
public abstract class Constants {

    public static final String FORM_FILE_NAME = "file";

    public static final String[] ALLOWED_FORMAT_TYPE = {"jpeg", "jpg", "png", "gif", "webp"};

    /**
     * 文件临时下载路径，目录不存在会报错，请事先建好
     */
    public static final String TMP = "/Users/rick/jkxyx205/tmp/fastdfs/tmp";

    /**
     * 缓存文件
     */
    public static final String CACHE = "/Users/rick/jkxyx205/tmp/fastdfs/tmp";

    /**
     * 缓存文件访问地址
     */
    public static final String CACHE_URL = "http://207.148.100.137:81/";

    /**
     * 图片自动压缩的阀值
     */
    public static double COMPRESS_THRESHOLD = 300 * 1024;
}
