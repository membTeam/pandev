package com.pandev.service.motification;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

@Component
public class NotificationFactory {
    private final Map<String, NotificationService> notificationServiceMap;

    public NotificationFactory(Map<String, NotificationService> notificationServices) {
        this.notificationServiceMap = notificationServices;
    }

    public NotificationService getNotificationService(String notificationType) {
        NotificationService notificationService = notificationServiceMap.get(notificationType);
        if (notificationService == null) {
            throw new RuntimeException("Unsupported notification type");
        }

        return notificationService;
    }

    public void execute(Message message) {
        var strType = NotificationType.getType(message);
        NotificationService notificationService = getNotificationService(strType);
        notificationService.applyMethod(message);
    }

}
