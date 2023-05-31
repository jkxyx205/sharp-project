package com.rick.demo.module.form.service;

import com.google.common.collect.Lists;
import com.rick.demo.module.form.entity.User;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictDOSupplier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Rick
 * @createdAt 2023-03-17 12:14:00
 */
@Component
public class DictDOSupplierImpl implements DictDOSupplier {
    @Override
    public List<Dict> get() {
//        - type: status
//        map: {LOCKED: ""锁定"", NORMAL: "正常"}
        //
//        List<Dict> list = Lists.newArrayListWithExpectedSize(User.StatusEnum.values().length + User.HobbyEnum.values().length);
//        list.add(new Dict("status", "LOCKED", "锁定", 0 ));
//        list.add(new Dict("status", "NORMAL", "正常", 1 ));

        List<Dict> list = Lists.newArrayListWithExpectedSize(User.HobbyEnum.values().length);
        int i = 0;
        for (User.HobbyEnum value : User.HobbyEnum.values()) {
            list.add(new Dict("hobbyList", value.name(), value.getLabel(), i++));
        }

        return list;
    }
}
