package com.rick.admin.module.code.entity;

import com.rick.db.repository.Table;
import com.rick.db.repository.model.EntityId;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2024/9/16 07:23
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_code_sequence", comment = "序号表")
public class CodeSequence extends EntityId<Long> {

    String category;

    String prefix;

    String name;

    Integer sequence;
}
