package com.pandev.controller;

import com.pandev.dto.DTOresult;
import com.pandev.service.excelService.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Path;

/**
 * Service API for all commands telegramBot.
 * Вспомогательный API for developer
 */
@Service
@RequiredArgsConstructor
@Log4j
public class MessageAPI {

    private SilentSender sender;
    private TelegramBot telegramBot;
    private final ExcelService excelService;

    public void init(SilentSender sender, TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        this.sender = sender;
    }

    /**
     * init SendMessage as default
     * @param chatId
     * @return SendMessage
     */
    public SendMessage initMessage(long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("empty")
                .build();
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
     *
     * @param chatId
     */
    public void unexpectedCommand(long chatId) {
        sendMessage(initMessage(chatId, "Команда не опознана."));
    }

    /**
     * Response message into telegramBot
     *
     * @param sendMessage
     */
    public void sendMessage(SendMessage sendMessage) {
        sender.execute(sendMessage);
    }

    public void sendMessage(long chatId, DTOresult dto) {
        if (!dto.res()) {
            sendMessage(initMessage(chatId, dto.mes()));
        } else {
            sendMessage((SendMessage) dto.value());
        }
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
                telegramBot.sender().sendDocument(((SendDocument) dto.value()));
            } else {
                throw new Exception(dto.mes());
            }
        } catch (Exception ex) {
            sendMessage(
                    initMessage(chatId, "Не известная ошибка загрузки документа.")
            );
        }
    }

    /**
     * Создание пояснительного сообщения для команды upload
     * @param chatId
     */
    public void replyToUpload(long chatId) {
        var text = "Вставьте файл Excel установленного образца.";
        var message = initMessage(chatId, text);
        sendMessage(message);
    }

    /**
     * Загрузка данных в формате Excel
     * Используется специальный шаблон: any-data/extenal-resource/test-upload-excel.xlsx
     * @param message
     */
    public DTOresult replyToUpload(Message message) {

        var resDTO = telegramBot.uploadDocument(message);

        if (!resDTO.res()) {
            return resDTO;
        }

        var strFile = ((Path) resDTO.value()).toString();

        DTOresult result;

        try {
            var lsData = excelService.readFromExcel(strFile);
            return excelService.saveDataByExcelToDb(lsData);

        } catch (Exception ex) {
            log.error(ex.getMessage());
            return DTOresult.err(ex.getMessage());
        }

    }

}
