package com.pandev.controller;

import com.pandev.service.strategyTempl.FactoryService;
import com.pandev.utils.FileAPI;
import com.pandev.utils.ParserMessage;
import com.pandev.service.excelService.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.pandev.utils.Constants.*;

@Service
@RequiredArgsConstructor
public class ResponseHandler {

    private final FileAPI fileAPI;
    private final FactoryService commBeanService;
    private final MessageAPI messageAPI;

    /**
     * Создание стартового сообщения
     * @param chatId
     */
    public void replyToStart(long chatId) {
        try {
            String text = fileAPI.loadDataFromFile(FILE_START);

            SendMessage message = messageAPI.initMessage(chatId, text);
            messageAPI.sendMessage(message);

        } catch (Exception ex) {
            messageAPI.sendMessage(messageAPI.initMessage(chatId, "Неизвестная ошибка. Зайдите позднее"));
        }

    }

    /**
     * Распределение входящих текстовых сообщений
     * @param update
     */
    public void replyToDistributionMess(Update update) {
        if (update != null && update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasDocument()) {
                commBeanService.responseToMessage(message);
                return;
            }

            var strCommand = ParserMessage.getstrCommandFromMessage(message);
            try {
                switch (strCommand) {
                    case COMD_START -> replyToStart(message.getChatId());
                    case COMD_ADD_ELEMENT, COMD_REMOVE_ELEMENT,
                         COMD_HELP, COMD_VIEW_TREE, COMD_DOWNLOAD ->
                            commBeanService.responseToMessage(message);
                    case COMD_UPLOAD -> messageAPI.infoMessageForUpload(message.getChatId());

                    default -> messageAPI.unexpectedCommand(message.getChatId());
                }
            } catch (Exception ex) {
                messageAPI.unexpectedCommand(message.getChatId());
            }
        }
    }

}
