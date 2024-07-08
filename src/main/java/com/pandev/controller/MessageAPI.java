package com.pandev.controller;


import com.pandev.utils.excelAPI.ExcelService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Value;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Service, used for send Response and init object SendMessage
 */
@Service
@Log4j
public class MessageAPI {

    private String excelFileName;

    private SilentSender sender;
    private TelegramBot telegramBot;

    private final ExcelService excelService;


    public MessageAPI(ExcelService excelService, @Value("${path-external-resource}") String excelFileName) {
        this.excelService = excelService;

        this.excelFileName = excelFileName;
    }

    public void init(SilentSender sender,  TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
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

    public void unexpectedCommand(long chatId) {
        sendMessage(initMessage(chatId, "Команда не опознана."));
    }

    /**
     * Response message into telegramBot
     * @param sendMessage
     */
    public void sendMessage(SendMessage sendMessage) {
            sender.execute(sendMessage);
    }


    public void downloadDocument(SendDocument document) {
        try {
            telegramBot.sender().sendDocument(document);
        } catch (Exception ex) {
            sendMessage(
                    MessageAPI.initMessage(Long.parseLong(document.getChatId()),
                            "Не известная ошибка загрузки документа.") );
        }
    }

    public void uploadDocument(Message message) {

        var document = message.getDocument();
        var chatId = message.getChatId();
        var fileId = document.getFileId();

        try {
            GetFile getFile = new GetFile(fileId);

            File file =  telegramBot.sender().execute(getFile);

            var strFile = "temp.xlsx";
            var pathExternale = Path.of(excelFileName, strFile);
            Files.deleteIfExists(pathExternale);

            java.io.File tempFile = new java.io.File(pathExternale.toAbsolutePath().toString());

            telegramBot.downloadFile(file, tempFile);

            var lsData = excelService.readFromExcel(strFile);
            excelService.saveDataByExcelToDb(lsData);

            sender.execute(
                    MessageAPI.initMessage(chatId,
                            "Выполнена загрузка данных из файла"));
        } catch (Exception ex) {
            sendMessage(
                    initMessage(chatId, "Не известная ошибка загрузки данных из Excel") );
        }
    }


}
