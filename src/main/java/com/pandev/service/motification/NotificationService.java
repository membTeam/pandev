package com.pandev.service.motification;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface NotificationService {
    void applyMethod(Message message);
}
