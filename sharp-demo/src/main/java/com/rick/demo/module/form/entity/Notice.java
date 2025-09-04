package com.rick.demo.module.form.entity;

import com.rick.db.repository.Table;
import com.rick.db.repository.Version;
import com.rick.db.repository.model.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

;

/**
 * @author Rick.Xu
 * @date 2023/5/12 15:10
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "t_notice", comment = "通知公告")
public class Notice extends BaseEntity<Long> {

    String name;

    @Version
    Integer version;

}