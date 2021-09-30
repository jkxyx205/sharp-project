package com.rick.fileupload.core.exception;

/**
 * @author Rick
 * @createdAt 2021-09-30 17:27:00
 */
public class NotImageTypeException extends RuntimeException {

    public NotImageTypeException() {
        super("文件不是图片类型");
    }
}
