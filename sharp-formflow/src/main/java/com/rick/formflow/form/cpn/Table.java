package com.rick.formflow.form.cpn;

import com.rick.formflow.form.cpn.core.AbstractCpn;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2021-11-02 17:11:00
 */
@Component
public class Table extends AbstractCpn<List<Object[]>> {

    @Override
    public CpnTypeEnum getCpnType() {
        return CpnTypeEnum.TABLE;
    }

//    @Override
//    public List parseStringValue(String value) {
//        try {
//            return StringUtils.isBlank(value) ? Collections.emptyList() : JsonUtils.toObject(value, List.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    public String getStringValue(List<Object[]>value) {
//        try {
//            return Objects.isNull(value) ? null : JsonUtils.toJson(value);
//        } catch (IOException e) {
//            return null;
//        }
//    }

    @Override
    public void valid(List<Object[]> value, String[] options) {
        // TODO 验证
    }

}
