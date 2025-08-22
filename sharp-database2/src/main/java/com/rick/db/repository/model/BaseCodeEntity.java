package com.rick.db.repository.model;

import com.rick.db.repository.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;

/**
 * 外部可见的唯一编号
 * @author Rick
 * @createdAt 2023-03-08 22:01:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseCodeEntity<ID> extends EntityIdCode<ID> implements AdditionalInfoGetter {

    @Valid
    @Embedded
    AdditionalInfo additionalInfo;
}
