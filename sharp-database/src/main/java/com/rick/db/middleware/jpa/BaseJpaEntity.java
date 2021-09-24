package com.rick.db.middleware.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rick.common.util.IdGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2021-09-23 23:10:00
 */
@Data
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
@JsonIgnoreProperties(value = {"handler","hibernateLazyInitializer","fieldHandler", "deleted"})
@EntityListeners(AuditingEntityListener.class)
public class BaseJpaEntity {

    @Id
    private Long id;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @JsonIgnore
    @Enumerated
    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted;

    @PrePersist
    public void prePersist() {
        if (Objects.isNull(id)) {
            id = IdGenerator.getId();
        }
        deleted = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (obj instanceof BaseJpaEntity) {
            BaseJpaEntity dataEntity = (BaseJpaEntity)obj ;
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
