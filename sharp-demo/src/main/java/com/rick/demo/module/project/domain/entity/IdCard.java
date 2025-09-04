package com.rick.demo.module.project.domain.entity;

import com.rick.db.repository.OneToMany;
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
 * @createdAt 2022-05-01 09:30:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("t_person_id_card")
public class IdCard extends BaseEntity<Long> {

    private String idNum;

    private String address;

    /**
     * 一对一
     * oneToOne = true
     */
    @OneToMany(joinColumnId = "id_card_id", oneToOne = true, mappedBy = "idCard")
    private Person person;

}
