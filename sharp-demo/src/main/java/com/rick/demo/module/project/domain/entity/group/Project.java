package com.rick.demo.module.project.domain.entity.group;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.TableName;
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
@TableName(value = "t_project2", subTables = {"t_project_detail2"})
@NoArgsConstructor
public class Project extends BasePureEntity {

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

    private PhoneNumber phoneNumber;

}
