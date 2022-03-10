package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.demo.module.project.domain.enums.SexEnum;
import com.rick.demo.module.project.domain.enums.UserStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author rick
 */
@SuperBuilder
@Getter
@Setter
@Table(value = "t_project1", subTables = {"t_project_detail1"}, comment = "任务")
@NoArgsConstructor
public class Project extends BasePureEntity {

    @NotBlank(message = "项目名称不能为空")
    @Column(comment="项目名称", nullable = true)
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

    @OneToMany(subTable = "t_project_detail1", reversePropertyName = "project", cascadeSaveOrUpdate = true, joinValue = "project_id")
    private List<ProjectDetail> projectDetailList;

    private Map<String, Object> map;

}
