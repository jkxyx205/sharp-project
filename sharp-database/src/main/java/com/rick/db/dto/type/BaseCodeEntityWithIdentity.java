package com.rick.db.dto.type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Id;
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
public class BaseCodeEntityWithIdentity extends BaseCodeEntity<Long> {

    @Id(strategy = Id.GenerationType.IDENTITY)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
}
