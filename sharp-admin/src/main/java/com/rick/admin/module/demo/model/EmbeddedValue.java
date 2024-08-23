package com.rick.admin.module.demo.model;

import com.rick.common.http.convert.JsonStringToObjectConverterFactory;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * pure对象 转 json 格式存储，如果是自己实现的类，尽量实现 JsonStringToObjectConverterFactory.JsonValue，第三方类可以不实现
 * @author Rick.Xu
 * @date 2024/8/19 03:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddedValue implements JsonStringToObjectConverterFactory.JsonValue {

    @DictType(type = "MATERIAL_TYPE") // 可以省略 从@Sql 获取label
    DictValue dictValue;

    String text;

}
