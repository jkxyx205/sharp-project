package com.rick.formflow.form.cpn.core;

import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
@Table(value = "sys_form_cpn_value")
public class FormCpnValue extends BaseEntity<Long> {

    @NotNull
    private Long formCpnId;

    @NotNull
    private Long formId;

    @NotNull
    private Long configId;

    @NotNull
    private Long instanceId;

    private String value;

}
