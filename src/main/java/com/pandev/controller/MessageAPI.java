package com.pandev.controller;

import com.pandev.dto.DTOresult;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Files;
import java.nio.file.Path;

import com.pandev.service.excelService.ExcelService;

/**
 * Service API for all commands telegramBot.
 * Вспомогательный API for developer
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
     *
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

    /**
     * Уведомление: команда не опознана
     * @param chatId
     */
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

    public void sendMessage(long chatId, DTOresult dto) {
        SendMessage sendMessage = dto.res()
                ? (SendMessage) dto.value()
                : initMessage(chatId, dto.mes());
        sendMessage(sendMessage);
    }

    /**
     * Выгрузка данных в формате Excel
     * используется специальный шаблон: any-data/extenal-resource/template.xlsx
     * long chatId
     * DTOresult dto
     */
    public void downloadDocument(long chatId, DTOresult dto) {
        try {
            if (dto.res()) {
                telegramBot.sender().sendDocument( ((SendDocument)dto.value()));
            } else {
                throw new Exception(dto.mes());
            }
        } catch (Exception ex) {
                    sendMessage(
                            initMessage(chatId, "Не известная ошибка загрузки документа.")
                    ); }
    }

    /**
     * Создание пояснительного сообщения для команды upload
     * @param chatId
     */
    public void infoMessageForUpload(long chatId) {
        var text = "Вставьте файл Excel установленного образца.";
        var message = initMessage(chatId, text);
        sendMessage(message);
    }

    /**
     * Загрузка данных в формате Excel
     * Используется специальный шаблон: any-data/extenal-resource/test-upload-excel.xlsx
     * @param message
     */
    public void infoMessageForUpload(Message message) {

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
                    initMessage(chatId,"Выполнена загрузка данных из файла"));

        } catch (Exception ex) {
            sendMessage(
                    initMessage(chatId, "Не известная ошибка загрузки данных из Excel") );
        }
    }


}
