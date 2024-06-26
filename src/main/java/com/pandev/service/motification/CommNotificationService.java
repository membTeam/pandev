package com.pandev.service.motification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class CommNotificationService {
    private final NotificationFactory notificationFactory;

    public void responseToMessage(Message message) {
        notificationFactory.execute(message);
    }

}
