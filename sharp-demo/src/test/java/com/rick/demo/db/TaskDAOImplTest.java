package com.rick.demo.db;

import com.google.common.collect.Lists;
import com.rick.db.service.support.Params;
import com.rick.demo.module.project.dao.TaskDAO;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.PhoneNumber;
import com.rick.demo.module.project.domain.entity.group.Task;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2022-03-22 12:26:00
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskDAOImplTest {

    @Autowired
    private TaskDAO taskDAO;

    @Test
    public void testSaveTask() {
        taskDAO.insert(Task.builder()
                .taskName("task A")
                .complete(true)
                .completeTime(LocalDateTime.now())
                .costHours(10)
                .assignUserId(1L)
                .status(UserStatusEnum.NORMAL)
        .address(Address.builder().code("001").detail("苏州").build())
        .list(Lists.newArrayList(Address.builder().code("001").detail("苏州").build()))
        .phoneNumber(PhoneNumber.builder().code("816").number("18888888888").build())
        .deleted(false)
        .map(Params.builder().pv("hello", "world").build())
                .build());
    }

    @Test
    public void testFindByJson() {
//        SELECT * FROM t_task WHERE address -> '$.code' = '001'
        List<Task> list = taskDAO.selectByParams(Params.builder(1).pv("code", "001").build(),
                "address -> '$.code' = :code");
        assertThat(CollectionUtils.isNotEmpty(list)).isEqualTo(true);
    }

    @Test
    public void testRegex() {
//        String sql = "SELECT task_name FROM t_task WHERE address -> '$.code'-> '$.code' = :code";
        String sql = "SELECT task_name FROM t_task WHERE address -> '$.code' = :code";
        Pattern pattern = Pattern.compile("((?i)(to_char|NVL)?\\s*([(][^([(]|[)])]*[)])|[a-zA-Z0-9'[.]_[-]]+)(\\s*->\\s*'\\$.[a-zA-Z]+\\w*')?\\s*(?i)(like|<>|!=|>=|<=|<|>|=|\\s+in|\\s+not\\s+in|regexp)\\s*(([(]\\s*:\\w+\\s*[)])|(:\\w+))");
        Matcher matcher = pattern.matcher(sql);
        matcher.find();
        String group = matcher.group();
//        System.out.println(group);

        String s = sql.replaceAll("(?s)((?i)((and|or)\\s+)|(,\\s*))?\\baddress -> '\\$\\.code' = :code(?!\\w+)", "");
        System.out.println(s);


//        String COLUMN_REGEX = "((?i)(to_char|NVL)?\\s*([(][^([(]|[)])]*[)])|[a-zA-Z0-9'[.]_[-]]+)(s*->\\s*'\\$\\.\\w+')?";
//        boolean matches = "address -> '$.code'".matches(COLUMN_REGEX);
//        System.out.println(matches);
    }
}
