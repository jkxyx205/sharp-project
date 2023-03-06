package com.rick.demo.module.embedded.entity;

import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.dao.annotation.Embedded;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.demo.module.embedded.model.ContactPerson;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
public class Company extends SimpleEntity {

    private String name;

    private String address;

    private String phone;

    @Embedded
    private ContactPerson contactPerson;

}
