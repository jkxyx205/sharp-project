package com.rick.admin.module.message.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * @author Rick.Xu
 * @date 2024/9/20 16:18
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "sys_message", comment = "消息")
@JsonIgnoreProperties("receiveUserIds")
public class Message extends BaseEntity<Long> {

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文
     */
    private String content;

    /**
     * 业务id
     */
    private Long businessId;

    /**
     * 业务实例id
     */
    private Long instanceId;

    /**
     * 消息接收人
     */
    @Column(value = "receive_user_id", columnDefinition = "text")
    private Set<Long> receiveUserIds;
}
