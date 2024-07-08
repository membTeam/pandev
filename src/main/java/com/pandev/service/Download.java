package com.pandev.service;

import com.pandev.controller.MessageAPI;
import com.pandev.service.strategyTempl.StrategyTempl;
import com.pandev.utils.excelAPI.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Path;


@Service
@RequiredArgsConstructor
public class Download implements StrategyTempl {

    private final MessageAPI messageAPI;
    private final ExcelService excelService;


    @Override
    public void applyMethod(Message message) {
        Path filePath;

        var resDTO =  excelService.downloadGroupsToExcel();
        long chatId = message.getChatId();

        if (!resDTO.res()) {
            messageAPI.sendMessage(
                    messageAPI.initMessage(chatId, "Не известная ошибка выгрузки файла Excel.") );
            return;
        }

        filePath = (Path) resDTO.value();

        var strPath = filePath.toAbsolutePath().toString();
        InputFile document = new InputFile(new java.io.File(strPath));

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setCaption("Дерево групп в формате Excel");
        sendDocument.setDocument(document);

        messageAPI.downloadDocument(sendDocument);

    }
}
