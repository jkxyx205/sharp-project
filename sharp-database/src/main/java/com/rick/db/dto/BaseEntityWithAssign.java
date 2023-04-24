package com.rick.db.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.Instant;

/**
 * @author Rick
 * @createdAt 2021-09-23 23:10:00
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class BaseEntityWithAssign {

    @Id(strategy = Id.GenerationType.ASSIGN)
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;

    @Column(updatable = false, comment = "创建人")
    private Long createId;

    @Column(updatable = false, comment = "创建时间")
    private Instant createTime;

    @Column(comment = "修改人")
    private Long updateId;

    @Column(comment = "修改时间")
    private Instant updateTime;

    @JsonIgnore
    @Column(value = SharpDbConstants.LOGIC_DELETE_COLUMN_NAME, comment = "是否逻辑删除")
    private Boolean deleted;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (obj instanceof BaseEntityWithAssign) {
            BaseEntityWithAssign dataEntity = (BaseEntityWithAssign)obj ;
            if (dataEntity.id != null && dataEntity.id.equals(id))
                return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id).toHashCode();
    }
}
