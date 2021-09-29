package com.rick.fileupload.persist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rick.db.config.annotation.TableName;
import com.rick.db.config.annotation.Transient;
import com.rick.fileupload.core.model.FileMeta;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

/**
 * @author Rick
 * @createdAt 2021-09-29 18:11:00
 */
@TableName("sys_document")
@Data
public class Document extends FileMeta {

    private Long id;

    private Instant createdAt;

    @Transient
    @JsonIgnore
    private byte[] data;

    /**
     * 获取完整名称
     * @return
     */
    public String getFullName() {
        if (StringUtils.isBlank(this.getExtension())) {
            return this.getName();
        }

        return this.getName() + (StringUtils.isEmpty(this.getExtension()) ? "" : "." + this.getExtension());
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
}
