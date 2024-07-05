package com.pandev.controller;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
@Log4j
public class MessageAPI {

    private MessageSender sender;

    public void init(MessageSender sender) {
        this.sender = sender;
    }

    public static SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes == null ? "empty" : mes)
                .build();
    }

    @SneakyThrows
    public void sendMessage(SendMessage sendMessage) {
            sender.execute(sendMessage);
    }

}
