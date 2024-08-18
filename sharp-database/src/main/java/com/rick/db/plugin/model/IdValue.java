package com.rick.db.plugin.model;

import com.rick.db.plugin.dao.annotation.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rick.Xu
 * @date 2023/7/27 21:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdValue {

    Long id;

    @Transient
    String description;

    public IdValue(String description) {
        this.description = description;
    }
}
