package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick
 * @createdAt 2022-06-15 00:52:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_farmer", comment = "农民")
public class Farmer extends BaseEntityWithLongId {

    private String name;
}