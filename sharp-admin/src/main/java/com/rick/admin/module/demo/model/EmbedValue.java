package com.rick.admin.module.demo.model;

import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Rick.Xu
 * @date 2024/8/19 03:32
 */
@Data
@AllArgsConstructor
public class EmbedValue {

    @DictType(type = "MATERIAL_TYPE") // 可以省略 从@Sql 获取label
    DictValue dictValue;

}
