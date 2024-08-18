package com.rick.db.plugin.model;

import com.rick.db.plugin.dao.annotation.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于 BaseCodeEntity 的关联，拥有 id ， 区别于DictValue
 * @author Rick.Xu
 * @date 2023/7/27 21:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdCodeValue {

    Long id;

    String code;

    @Transient
    String description;

    public IdCodeValue(String code) {
        this.code = code;
    }

    public IdCodeValue(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
