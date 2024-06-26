package com.pandev.utils;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageAPI {



    public static SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes == null ? "empty" : mes)
                .build();
    }
}
