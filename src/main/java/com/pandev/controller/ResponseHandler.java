package com.pandev.controller;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.nio.file.Path;

import com.pandev.templCommand.CommCommand;
import com.pandev.utils.FileAPI;
import com.pandev.utils.ParserMessage;
import com.pandev.utils.excelAPI.ExcelService;
import static com.pandev.utils.Constants.*;


@Service
public class ResponseHandler {

    private final CommCommand commCommand;
    private final FileAPI fileAPI;
    private SilentSender sender;
    private final ExcelService excelService;
    private TelegramBot telegramBot;

    public ResponseHandler(CommCommand commCommand, FileAPI fileAPI, ExcelService excelService) {
        this.commCommand = commCommand;
        this.fileAPI = fileAPI;

        this.excelService = excelService;
    }

    @PostConstruct
    private void init() {
        commCommand.init(this);
    }

    public void init(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        this.sender = telegramBot.silent();
    }

    /**
     * Создание сообщения, если команда Не опознанная
     * @param chatId
     */
    private void unexpectedCommand(long chatId) {
        sender.execute(initMessage(chatId, "Команда не опознана."));
    }


    /**
     * Создание стартового сообщения
     * @param chatId
     */
    public void replyToStart(long chatId) {
        try {
            String file = FILE_START_REGISTER_USER;
            String text = fileAPI.loadDataFromFile(file);

            SendMessage message = initMessage(chatId, text);
            sender.execute(message);

        } catch (Exception ex) {
            sender.execute(
                    initMessage(chatId, "Неизвестная ошибка. Зайдите позднее"));
        }

    }

    /**
     * Распределение входящих текстовых сообщений
     * @param update
     */
    public void replyToDistributionMess(Update update) {
        if (update != null && update.hasMessage()) {
            Message message = update.getMessage();

            var strCommand = ParserMessage.getstrCommandFromMessage(message);
            try {
                switch (strCommand) {
                    case COMD_START -> replyToStart(message.getChatId());
                    case COMD_ADD_ELEMENT, COMD_REMOVE_ELEMENT,
                         COMD_HELP, COMD_VIEW_TREE ->
                           sender.execute(commCommand.initMessageFromStrCommand(message));
                    case COMD_DOWNLOAD -> replyToDownload(message.getChatId());
                    case COMD_UPLOAD -> replyToUpload(message.getChatId());

                    default -> unexpectedCommand(message.getChatId());
                }
            } catch (Exception ex) {
                unexpectedCommand(message.getChatId());
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
        sender.execute(
                initMessage(chatId, text) );
    }

    /**
     * Создание и выгрузка файла Excel.
     * Создается в соответствии с шаблоном any-data/extenal-resource/template.xlsx.
     * Метод excelService.writeGroupsToExcel() создает файл any-data/extenal-resource/download.xlsx,
     * который используется для download
     * @param chatId
     */
    private void replyToDownload(long chatId) {
        var resDTO =  excelService.downloadGroupsToExcel();

        if (!resDTO.res()) {
            sender.execute(
                    initMessage(chatId, "Не известная ошибка загрузки файла.") );
            return;
        }

        var strPath = ((Path) resDTO.value()).toAbsolutePath().toString();
        InputFile document = new InputFile(new java.io.File(strPath));

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setCaption("Дерево групп в формате Excel");
        sendDocument.setDocument(document);

        try {
            telegramBot.sender().sendDocument(sendDocument);
        } catch (Exception ex) {
            sender.execute(
                    initMessage(chatId, "Не известная ошибка загрузки документа.") );
        }

    }

    /**
     * Создание шаблона текстового сообщения.
     * @param chatId
     * @param mes
     * @return
     */
    public SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes == null ? "empty" : mes)
                .build();
    }

}
