package com.rick.demo.module.project.domain.entity;

import com.rick.db.repository.Column;
import com.rick.db.repository.OneToMany;
import com.rick.db.repository.Table;
import com.rick.db.repository.ToStringValue;
import com.rick.db.repository.model.BaseEntity;
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

;

/**
 * @author rick
 */
@SuperBuilder
@Getter
@Setter
@Table(value = "t_project1", comment = "任务")
@NoArgsConstructor
public class Project extends BaseEntity<Long> {

    @NotBlank(message = "项目名称不能为空")
    @Column(comment="项目名称")
    private String title;

    private String description;

    private String coverUrl;

    private Long ownerId;

    private SexEnum sex;

    private Address address;

    @NotNull
    private UserStatusEnum status;

    private List<Address> list;

    /**
     * 通过 StringToPhoneNumberConverterFactory 解析到 entity
     * @ToStringValue 使用ToString() 保存数据，否则使用 toJson
     */
    @ToStringValue
    private PhoneNumber phoneNumber;

    @OneToMany(mappedBy = "project", joinColumnId = "project_id")
    private List<ProjectDetail> projectDetailList;

    private Map<String, Object> map;

}
