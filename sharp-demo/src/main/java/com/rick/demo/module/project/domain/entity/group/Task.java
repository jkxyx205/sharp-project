package com.rick.demo.module.project.domain.entity.group;

import com.rick.db.repository.Column;
import com.rick.db.repository.Table;
import com.rick.db.repository.ToStringValue;
import com.rick.db.repository.model.BaseEntity;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.Dept;
import com.rick.demo.module.project.domain.entity.PhoneNumber;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

;

/**
 * @author Rick
 * @createdAt 2022-03-01 14:24:00
 */
@SuperBuilder
@Getter
@Setter
@Table(value = "t_task", comment = "任务表")
@NoArgsConstructor
public class Task extends BaseEntity<Long> {

    @Column(nullable = true, comment = "任务名称")
    private String taskName;

    @Column(comment = "完成时间")
    private LocalDateTime completeTime;

    private Integer costHours;

    private Boolean complete;

    private Long assignUserId;

    private Address address;

    @NotNull
    @Column(comment = "用户状态")
    private UserStatusEnum status;

    private List<Address> list;

    @ToStringValue
    private PhoneNumber phoneNumber;

    private Map<String, Object> map;

    private List<Map<String, Object>> listMap;

    private List<Integer> numList;

    private List<String> strList;

    /**
     * 没有实现JsonStringToObjectConverterFactory.JsonValue
     * 重写 toString()，可以持久到数据库， 不能反序列化，需要定义转换器 DeptConverterFactory
     *
     */
    private Dept dept;

}
