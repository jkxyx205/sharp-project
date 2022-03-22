package com.rick.demo.module.project.domain.entity.group;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.demo.module.project.domain.entity.Address;
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

/**
 * @author Rick
 * @createdAt 2022-03-01 14:24:00
 */
@SuperBuilder
@Getter
@Setter
@Table(value = "t_task", comment = "任务表")
@NoArgsConstructor
public class Task extends BasePureEntity {

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

    private PhoneNumber phoneNumber;

    private Map<String, Object> map;

}
