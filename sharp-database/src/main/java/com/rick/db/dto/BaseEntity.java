package com.rick.db.dto;

import com.rick.db.constant.SharpDbConstants;
import com.rick.db.plugin.dao.annotation.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * @author Rick
 * @createdAt 2021-09-23 23:10:00
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class BaseEntity extends SimpleEntity {

    @Column(updatable = false)
    private Long createdBy;

    @Column(updatable = false)
    private Instant createdAt;

    private Long updatedBy;

    private Instant updatedAt;

    @Column(SharpDbConstants.LOGIC_DELETE_COLUMN_NAME)
    private Boolean deleted;

}
