package com.rick.demo.module.form.service;

import com.google.common.collect.Lists;
import com.rick.demo.module.form.entity.User;
import com.rick.meta.dict.dao.dataobject.DictDO;
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
    public List<DictDO> get() {
//        - type: status
//        map: {LOCKED: ""锁定"", NORMAL: "正常"}
        //
//        List<DictDO> list = Lists.newArrayListWithExpectedSize(User.StatusEnum.values().length + User.HobbyEnum.values().length);
//        list.add(new DictDO("status", "LOCKED", "锁定", 0 ));
//        list.add(new DictDO("status", "NORMAL", "正常", 1 ));

        List<DictDO> list = Lists.newArrayListWithExpectedSize( User.HobbyEnum.values().length);
        int i = 0;
        for (User.HobbyEnum value : User.HobbyEnum.values()) {
            list.add(new DictDO("hobbyList", value.name(), value.getLabel(), i++ ));
        }

        return list;
    }
}
