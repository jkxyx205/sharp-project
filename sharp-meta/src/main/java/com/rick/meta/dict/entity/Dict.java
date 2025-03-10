package com.rick.meta.dict.entity;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2023/5/31 15:02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "sys_dict", comment = "字典表")
public class Dict extends BaseEntity<Long> {

    String type;

    String name;

    String label;

    Integer sort;

    String remark;

    public Dict(String type, String name, String label, Integer sort) {
        this(type, name, label, sort, null);
    }

}