package com.rick.db.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class BaseEntity extends SimpleEntity {

    @Column(updatable = false, comment = "创建人")
    private Long createId;

    @Column(updatable = false, comment = "创建时间")
    private LocalDateTime createTime;

    @Column(comment = "修改人")
    private Long updateId;

    @Column(comment = "修改时间")
    private LocalDateTime updateTime;

    @JsonIgnore
    @Column(value = SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, comment = "是否逻辑删除")
    private Boolean deleted;

}
