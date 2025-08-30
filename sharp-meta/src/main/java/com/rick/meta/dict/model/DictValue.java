package com.rick.meta.dict.model;

import com.rick.db.repository.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author Rick.Xu
 * @date 2024/8/19 01:40
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DictValue {

    /**
     * 对应 Dict 中的字段 name，必须是 code 而非 name 是那么是因为需要通过 EntityWithCodePropertyDeserializer 注入值
     */
    String code;

    @Transient
    String label;

    @Transient
    String type;

    public DictValue(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public DictValue(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
