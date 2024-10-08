package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.type.BaseEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.ToStringValue;
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
@Table(value = "t_project1", comment = "任务")
@NoArgsConstructor
public class Project extends BaseEntityWithLongId {

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

    @OneToMany(subTable = "t_project_detail1", reversePropertyName = "project", cascadeInsertOrUpdate = true, joinValue = "project_id")
    private List<ProjectDetail> projectDetailList;

    private Map<String, Object> map;

}
