package com.rick.demo.module.embedded.entity;

import com.rick.db.repository.Embedded;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.EntityId;
import com.rick.demo.module.embedded.model.ContactPerson;
import lombok.*;
import lombok.experimental.SuperBuilder;

;

/**
 * @author Rick
 * @createdAt 2023-03-06 13:32:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_company", comment = "公司")
@ToString
public class Company extends EntityId<Long> {

    private String name;

    private String address;

    private String phone;

    @Embedded
    private ContactPerson contactPerson;

}
