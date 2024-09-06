package com.rick.db.dto.type;

import com.rick.db.dto.BaseCodeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick
 * @createdAt 2021-09-23 23:10:00
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class BaseCodeEntityWithStringId extends BaseCodeEntity<Long> {

}
