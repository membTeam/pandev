package com.pandev.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.springframework.stereotype.Service;


/**
 * Service, used for send Response and init object SendMessage
 */
@Service
@RequiredArgsConstructor
@Log4j
public class MessageAPI {

    private SilentSender sender;

    public void init(SilentSender sender) {
        this.sender = sender;
    }


    /**
     * init object SendMessage as default
     * @param chatId
     * @param mes
     * @return
     */
    public static SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes == null ? "empty" : mes)
                .build();
    }

    /**
     * Response message into telegramBot
     * @param sendMessage
     */
    public void sendMessage(SendMessage sendMessage) {
            sender.execute(sendMessage);
    }

}
