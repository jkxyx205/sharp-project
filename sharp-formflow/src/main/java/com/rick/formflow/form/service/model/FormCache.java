package com.rick.formflow.form.service.model;

import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.cpn.core.FormCpn;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2024/8/25 18:03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FormCache {

    Form form;

    List<FormCpn> formCpnList;

    Map<Long, CpnConfigurer> configIdMap;

}
