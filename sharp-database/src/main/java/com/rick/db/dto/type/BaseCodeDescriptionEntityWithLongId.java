package com.rick.db.dto.type;

import com.rick.db.dto.BaseCodeDescriptionEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2024/9/6 05:41
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class BaseCodeDescriptionEntityWithLongId extends BaseCodeDescriptionEntity<Long> {
}
