package com.rick.fileupload.client.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.fileupload.core.model.FileMeta;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Rick
 * @createdAt 2021-09-29 18:11:00
 */
@Table("sys_document")
@Data
public class Document extends FileMeta {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private LocalDateTime createTime;

}
