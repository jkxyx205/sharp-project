package com.rick.db.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.config.annotation.ColumnName;
import com.rick.db.config.annotation.Id;
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
public class BasePureEntity {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ColumnName(value = "created_by", updatable = false)
    private Long createdBy;

    @ColumnName(value = "created_at",  updatable = false)
    private Instant createdAt;

    private Long updatedBy;

    private Instant updatedAt;

    @ColumnName("is_deleted")
    @JsonIgnore
    private Boolean deleted;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (obj instanceof BasePureEntity) {
            BasePureEntity dataEntity = (BasePureEntity)obj ;
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
