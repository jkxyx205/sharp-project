package com.rick.db.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.repository.Column;
import com.rick.db.repository.support.Constants;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Rick.Xu
 * @date 2025/8/22 14:52
 */
@Setter
@Getter
@EqualsAndHashCode
public class BaseEntityInfo implements Serializable {

    @Column(updatable = false, comment = "创建人")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long createBy;

    @Column(updatable = false, nullable = false, comment = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;

    @Column(comment = "更新人")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long updateBy;

    @Column(nullable = false, comment = "更新时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updateTime;

    @JsonIgnore
    @Column(value = Constants.LOGIC_DELETE_COLUMN_NAME, nullable = false, comment = "是否逻辑删除")
    private Boolean deleted;
}
