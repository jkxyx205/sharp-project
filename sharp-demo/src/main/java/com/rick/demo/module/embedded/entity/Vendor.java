package com.rick.demo.module.embedded.entity;

import com.rick.db.repository.Table;
import com.rick.db.repository.model.EntityId;
import lombok.*;
import lombok.experimental.SuperBuilder;

;

/**
 * @author Rick
 * @createdAt 2023-03-06 17:53:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_company_vendor", comment = "供应商")
@ToString
public class Vendor extends EntityId<Long> {

    private String name;

    private Long companyId;
}
