package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.TableName;
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
@TableName(value = "t_project1", subTables = {"t_project_detail1"})
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

    @OneToMany(subTable = "t_project_detail1", reversePropertyName = "project")
    private List<ProjectDetail> projectDetailList;

}
