package com.rick.demo.module.embedded.model;

import com.rick.db.repository.Column;
import com.rick.db.repository.ManyToOne;
import com.rick.db.repository.OneToMany;
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

    @OneToMany(joinColumnId = "company_id", mappedBy = "companyId")
    private List<Vendor> vendorList;

    @ManyToOne(value = "role_id", cascadeSave = true)
    private Role role;
}
