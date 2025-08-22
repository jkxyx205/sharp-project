package com.rick.db.repository.model;

import com.rick.db.repository.Embedded;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;

/**
 * @author Rick
 * @createdAt 2021-09-23 23:10:00
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class BaseEntity<ID> extends EntityId<ID> implements AdditionalInfoGetter {

    @Valid
    @Embedded
    AdditionalInfo additionalInfo;

}
