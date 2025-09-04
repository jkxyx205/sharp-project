package com.rick.demo.module.project.domain.entity;

import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

;

/**
 * @author Rick
 * @createdAt 2022-06-15 01:17:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_material", comment = "农民")
public class Material extends BaseEntity<String> {

    private String name;
}
