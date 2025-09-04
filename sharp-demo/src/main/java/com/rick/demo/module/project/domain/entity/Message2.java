package com.rick.demo.module.project.domain.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rick.db.repository.Id;
import com.rick.db.repository.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
@Table(value = "t_message2", comment = "消息2")
public class Message2 {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long seq;

    private String text;

}
