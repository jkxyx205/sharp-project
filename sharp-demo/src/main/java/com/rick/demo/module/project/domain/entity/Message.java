package com.rick.demo.module.project.domain.entity;

import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

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
public class Message extends SimpleEntity<Long> {

    private String text;

    @Column(updatable = false)
    private Instant createdAt;
}
