package com.rick.db.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.plugin.dao.annotation.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * @author Rick
 * @createdAt 2021-09-23 23:10:00
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class SimpleBaseEntity<ID> extends SimpleEntity<ID> {

    @Column(updatable = false, comment = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;

    @Column(comment = "更新时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updateTime;

    @JsonIgnore
    @Column(value = SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, comment = "是否逻辑删除")
    private Boolean deleted;

}
