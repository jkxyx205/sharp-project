package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick
 * @createdAt 2022-03-03 19:26:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("t_role")
@ToString
public class Role extends BaseEntityWithLongId {

    private String name;

}