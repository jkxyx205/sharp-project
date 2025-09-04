package com.rick.demo.module.project.domain.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.common.http.json.deserializer.EntityWithLongIdPropertyDeserializer;
import com.rick.db.repository.ManyToMany;
import com.rick.db.repository.ManyToOne;
import com.rick.db.repository.Select;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
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
public class Person extends BaseEntity<Long> {

    private String name;

    private String sex;

    @ManyToMany(tableName = "t_person_role", inverseJoinColumnId = "role_id", joinColumnId = "person_id")
    private List<Role> roleList;

    @JsonAlias({"idCard", "id_card_id"})
    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @ManyToOne(value = "id_card_id", cascadeSave = true)
    private IdCard idCard;

    @Select("select * from t_role")
    private List<Role> roleAll;

}