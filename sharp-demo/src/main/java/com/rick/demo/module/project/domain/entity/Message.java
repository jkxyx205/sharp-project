package com.rick.demo.module.project.domain.entity;

import com.rick.db.repository.Column;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

;

/**
 * @author Rick
 * @createdAt 2022-10-28 19:02:00
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_message", comment = "消息")
public class Message extends EntityId<Long> {

    private String text;

    @Column(updatable = false)
    private Instant createdAt;
}
