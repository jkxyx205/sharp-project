package com.rick.fileupload.client.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Id;
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

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Column(updatable = false, comment = "创建人")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long createBy;

    @Column(updatable = false, comment = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;

    @Column(comment = "更新人")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long updateBy;

    @Column(comment = "更新时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updateTime;

    @JsonIgnore
    @Column(value = SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, comment = "是否逻辑删除")
    private Boolean deleted;

}
