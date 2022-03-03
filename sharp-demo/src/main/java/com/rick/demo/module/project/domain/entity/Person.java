package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-03-03 19:26:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("t_person")
public class Person extends BasePureEntity {

    private String name;

    @ManyToMany(thirdPartyTable = "t_person_role", referenceColumnName = "role_id", columnDefinition = "person_id", referenceTable = "t_role")
    private List<Role> roleList;

}