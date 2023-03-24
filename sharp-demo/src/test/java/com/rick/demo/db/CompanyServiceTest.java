package com.rick.demo.db;

import com.rick.demo.module.embedded.dao.CompanyDAO;
import com.rick.demo.module.embedded.entity.Company;
import com.rick.demo.module.embedded.entity.Vendor;
import com.rick.demo.module.embedded.model.ContactPerson;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2023-03-06 14:17:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompanyServiceTest {
    @Autowired
    private CompanyDAO companyDAO;

    @Test
    public void testSave() {
        companyDAO.insert(Company.builder()
                .name("云")
                .phone("110")
                .address("Nanjing")
                .contactPerson(ContactPerson.builder()
                        .firstName("Rick")
                        .lastName("Xu")
                        .phone("120")
//                        .vendorList(Arrays.asList(
//                                Vendor.builder().name("VE_CO").build(),
//                                Vendor.builder().name("BUILD").build()
//                                )
//                        )
//                        .role(Role.builder().name("Admin").build())
                        .build())
                .build());
    }

    @Test
    public void testUpdate() {
        companyDAO.update(Company.builder()
                .id(664989107511951360L)
                .name("点")
                .phone("110")
                .address("Nanjing")
                .contactPerson(ContactPerson.builder()
                        .firstName("Jim")
                        .lastName("Green")
                        .phone("119")
                        .vendorList(Arrays.asList(
                                Vendor.builder().name("BUILD").build(),
                                Vendor.builder().name("DREAM").build()
                                )
                        )
                        .build())
                .build());
    }

    @Test
    public void testFindById() {
        List<Company> list = companyDAO.selectByIds(664989107511951360L);
        list.forEach(System.out::println);
    }

    @Test
    public void testFindById2() {
        Company company = companyDAO.selectById(664989107511951360L).get();
        assertThat(company.getContactPerson().getFirstName()).isEqualTo("Jim");
        assertThat(company.getContactPerson().getVendorList().get(0).getName()).isEqualTo("BUILD");
    }
}