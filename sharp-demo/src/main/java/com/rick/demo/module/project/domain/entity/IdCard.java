package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick
 * @createdAt 2022-05-01 09:30:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("t_person_id_card")
public class IdCard extends BasePureEntity {

    private String idNum;

    private String address;

    /**
     * 一对一
     * oneToOne = true
     */
    @OneToMany(subTable = "t_person", joinValue = "id_card_id", cascadeSaveOrUpdate = true, oneToOne = true, reversePropertyName = "idCard")
    private Person person;
}
