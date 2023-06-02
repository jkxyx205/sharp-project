package com.rick.formflow.form.cpn.core;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-03 17:07:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "sys_form_cpn_configurer")
public class FormCpn extends BaseEntity {

    @NotNull
    private Long formId;

//    @NotBlank
//    private String name;

    @NotNull
    private Long configId;

    private Integer orderNum;

    private Map<String, Object> additionalInfo;
    
}
