package com.rick.db.dto;

import com.rick.db.plugin.dao.annotation.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 外部可见的唯一编号
 * @author Rick
 * @createdAt 2023-03-08 22:01:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseCodeEntity<ID> extends BaseEntity<ID> {

    @Length(max = 32, message = "编号不能超过32个字符")
    @NotNull
    @Pattern(regexp = "^[0-9a-zA-Z_/%-]{1,}$", message = "CODE只能包含数字、字母、下划线、中划线")
    @Column(value = "code", updatable = false, comment = "外部可见，唯一code", nullable = false)
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
