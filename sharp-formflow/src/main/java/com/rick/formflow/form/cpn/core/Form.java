package com.rick.formflow.form.cpn.core;

import com.rick.db.dto.BasePureEntity;
import com.rick.db.plugin.dao.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * @author Rick
 * @createdAt 2021-11-03 17:07:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@TableName(value = "sys_form")
public class Form extends BasePureEntity {

    @NotBlank
    private String name;

}
