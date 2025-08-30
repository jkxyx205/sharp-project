package com.rick.db.repository.model;

import com.rick.db.repository.Transient;
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
