package com.rick.demo.module.book.entity;

import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseCodeEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

;

/**
 * @author Rick.Xu
 * @date 2023/10/17 12:22
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "plant_storage_location", comment = "库位")
public class StorageLocation extends BaseCodeEntity<Long> {

    String description;

    Long plantId;

}