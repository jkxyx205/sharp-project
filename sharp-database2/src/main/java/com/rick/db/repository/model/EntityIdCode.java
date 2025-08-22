package com.rick.db.repository.model;

import com.rick.db.repository.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author Rick.Xu
 * @date 2025/8/14 11:46
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityIdCode<ID> extends EntityId<ID> {

    @Length(max = 32, message = "编号不能超过32个字符")
    @NotBlank
    @Pattern(regexp = "^[0-9a-zA-Z_/%-]{1,}$", message = "CODE只能包含数字、字母、下划线、中划线")
    @Column(value = "code", comment = "外部可见，唯一code", nullable = false)
    private String code;

    @Override
    public boolean equals(Object obj) {
        boolean isEquals = super.equals(obj);
        if (!isEquals) {
            if (obj == null || getClass() != obj.getClass()) return false;

            if (obj instanceof BaseCodeEntity) {
                BaseCodeEntity dataEntity = (BaseCodeEntity)obj ;
                if (StringUtils.isNotBlank(code) && code.equals(dataEntity.getCode())) {
                    return true;
                }

            }
        }

        return isEquals;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.getId()).append(this.code).toHashCode();
    }
}
