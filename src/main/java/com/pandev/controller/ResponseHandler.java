package com.pandev.controller;

import com.pandev.service.strategyTempl.FactoryService;
import com.pandev.utils.FileAPI;
import com.pandev.utils.ParserMessage;
import com.pandev.utils.excelAPI.ExcelService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.nio.file.Path;

import static com.pandev.utils.Constants.*;

@Service
public class ResponseHandler {

    private final FileAPI fileAPI;
    private final ExcelService excelService;
    private TelegramBot telegramBot;
    private final FactoryService commBeanService;
    private final MessageAPI messageAPI;


    public ResponseHandler(FileAPI fileAPI,
                           ExcelService excelService, FactoryService commBeanService, MessageAPI messageAPI) {
        this.fileAPI = fileAPI;

        this.excelService = excelService;
        this.commBeanService = commBeanService;
        this.messageAPI = messageAPI;
    }

    public void init(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * Создание стартового сообщения
     * @param chatId
     */
    public void replyToStart(long chatId) {
        try {
            String file = FILE_START_REGISTER_USER;
            String text = fileAPI.loadDataFromFile(file);

            SendMessage message = MessageAPI.initMessage(chatId, text);
            messageAPI.sendMessage(message);

        } catch (Exception ex) {
            messageAPI.sendMessage(MessageAPI.initMessage(chatId, "Неизвестная ошибка. Зайдите позднее"));
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

                    default -> messageAPI.unexpectedCommand(message.getChatId());
                }
            } catch (Exception ex) {
                messageAPI.unexpectedCommand(message.getChatId());
            }
        }
    }

    /**
     * Сообщение с вспомогательным текстом как загружать Excel документ
     * @param chatId
     */
    private void replyToUpload(long chatId) {
        var text = "Для загрузки данных из Excel используется специальный шаблон.\n" +
                "Вставьте документ Excel.";
        messageAPI.sendMessage(MessageAPI.initMessage(chatId, text) );
    }


}
