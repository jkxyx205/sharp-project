package com.rick.demo.module.project.domain.entity.group;

import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.ToStringValue;
import com.rick.demo.module.project.domain.entity.Address;
import com.rick.demo.module.project.domain.entity.PhoneNumber;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author rick
 */
@SuperBuilder
@Getter
@Setter
@Table(value = "t_project2")
@NoArgsConstructor
public class Project extends BaseEntityWithLongId {

    @NotBlank(message = "项目名称不能为空")
    private String title;

    private String description;

    private String coverUrl;

    private Long ownerId;

    private SexEnum sex;

    private Address address;

    @NotNull
    private UserStatusEnum status;

    private List<Address> list;

    @ToStringValue
    private PhoneNumber phoneNumber;

}
