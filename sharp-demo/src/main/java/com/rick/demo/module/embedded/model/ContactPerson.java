package com.rick.demo.module.embedded.model;

import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.demo.module.embedded.entity.Vendor;
import com.rick.demo.module.project.domain.entity.Role;
import lombok.*;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2023-03-06 13:34:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ContactPerson {

    private String firstName;

    @Column(updatable = false)
    private String lastName;

    @Column("contact_phone")
    private String phone;

    @OneToMany(subTable = "t_company_vendor", joinValue = "company_id", cascadeInsertOrUpdate = true, reversePropertyName = "companyId")
    private List<Vendor> vendorList;

    @ManyToOne(value = "role_id", parentTable = "t_role", cascadeInsertOrUpdate = true)
    private Role role;
}
