package com.pandev.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface NotificationService {
    void applyMethod(Message message);
}
