package com.pandev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class CommNotificationService {
    private final NotificationFactory notificationFactory;

    public SendMessage responseToMessage(Message message) {
        return notificationFactory.execute(message);
    }

}
