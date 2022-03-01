package com.rick.formflow.form.cpn.core;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class FormCpn extends BasePureEntity {

    @NotNull
    private Long formId;

    @NotBlank
    private String name;

    @NotNull
    private Long configId;

    private Integer orderNum;
    
}
