package com.rick.demo.module.project.domain.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rick.common.http.json.deserializer.EntityWithLongIdPropertyDeserializer;
import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.Sql;
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
public class Person extends BaseEntityWithLongId {

    private String name;

    private String sex;

    @ManyToMany(thirdPartyTable = "t_person_role", referenceColumnName = "role_id", columnDefinition = "person_id", referenceTable = "t_role")
    private List<Role> roleList;

    @JsonAlias({"idCard", "id_card_id"})
    @JsonDeserialize(using = EntityWithLongIdPropertyDeserializer.class)
    @ManyToOne(parentTable = "t_person_id_card", value = "id_card_id", cascadeInsertOrUpdate = true)
    private IdCard idCard;

    @Sql("select * from t_role")
    private List<Role> roleAll;

}