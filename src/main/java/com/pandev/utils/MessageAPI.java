package com.pandev.utils;


import com.pandev.controller.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
@Log4j
public class MessageAPI {

    private TelegramBot telegramBot;

    public void init(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public static SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes == null ? "empty" : mes)
                .build();
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            telegramBot.sender().execute(sendMessage);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

}
