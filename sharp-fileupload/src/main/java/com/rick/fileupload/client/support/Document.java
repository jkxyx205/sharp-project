package com.rick.fileupload.client.support;

import com.rick.db.config.annotation.TableName;
import com.rick.fileupload.core.model.FileMeta;
import lombok.Data;

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

}
