package com.rick.admin.core;

import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.SharpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2024/9/14 17:17
 */
@SpringBootTest
public class CpnCleanTest {

    @Autowired
    private SharpService sharpService;

    @Test
    public void testClean() {
        while (true) {
            Optional<String> optional = sharpService.queryForObject("select GROUP_CONCAT(id) ids from sys_form_cpn_configurer where not exists (select 1 from sys_form where sys_form.id = sys_form_cpn_configurer.form_id)", null, String.class);
            if (optional.isPresent()) {
                String deletedIds = optional.get();
                System.out.println("sys_form_cpn_configurer delete count = " + SQLUtils.delete("sys_form_cpn_configurer", "id", deletedIds));
            } else {
                break;
            }
        }

        while (true) {
            Optional<String> optional = sharpService.queryForObject("select GROUP_CONCAT(id) ids from sys_form_configurer where not exists (select 1 from sys_form_cpn_configurer where sys_form_cpn_configurer.config_id = sys_form_configurer.id)", null, String.class);
            if (optional.isPresent()) {
                String deletedIds = optional.get();
                System.out.println("sys_form_configurer delete count = " + SQLUtils.delete("sys_form_configurer", "id", deletedIds));
            } else {
                break;
            }
        }
    }
}
