package com.rick.fileupload.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rick.common.util.StringUtils;
import com.rick.db.config.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Rick
 * @createdAt 2021-09-29 13:50:00
 */
@Getter
@Setter
public class FileMeta {

    private String name;

    private String extension;

    private String contentType;

    private Long size;

    private String groupName;

    private String path;

    /**
     * data == groupName + path
     * 获取文件数据
     */
    @Transient
    @JsonIgnore
    private byte[] data;

    @Transient
    private String url;

    /**
     * 获取完整名称
     * @return
     */
    public String getFullName() {
        return StringUtils.fullName(name, extension);
    }

    public String getFullPath() {
        return this.getGroupName() + "/" + getPath();
    }

    /**
     * 设置完整名称
     * @param fullName
     */
    public void setFullName(String fullName) {
        String fileName = com.rick.common.util.StringUtils.stripFilenameExtension(fullName);
        String fileExt = com.rick.common.util.StringUtils.getFilenameExtension(fullName);
        setName(fileName);
        setExtension(fileExt);
    }

    public Long getSize() {
        if (size == null && data != null) {
            return (long) data.length;
        }

        return size;
    }

}
