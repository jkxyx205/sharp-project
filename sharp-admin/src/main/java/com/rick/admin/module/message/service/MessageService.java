package com.rick.admin.module.message.service;

import com.rick.admin.module.message.entity.Message;
import com.rick.db.plugin.dao.core.EntityDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Rick.Xu
 * @date 2024/9/20 16:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final EntityDAO<Message, Long> messageDAO;

    /**
     * 添加消息
     * @param message
     * @return
     */
    public Long addMessage(Message message) {
        for (Long userId : message.getReceiveUserIds()) {
            log.info("MESSAGE: send Message to {} with [{}]", userId, message.getTitle());
            simpMessagingTemplate.convertAndSendToUser(userId.toString(), "/queue/message", message);
        }
        messageDAO.insert(message);
        return message.getId();
    }
}
