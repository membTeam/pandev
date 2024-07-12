package com.pandev.service.commands;

import com.pandev.controller.MessageAPI;
import com.pandev.dto.DTOresult;
import com.pandev.service.strategyTempl.StrategyTempl;
import com.pandev.service.excelService.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Path;

/**
 * Сервис команды /download выгрузка данных из БД в формат Excel
 * Используется специальный шаблон: any-data/extenal-resource/template.xlsx
 */
@Service
@RequiredArgsConstructor
public class Download implements StrategyTempl {

    private final ExcelService excelService;


    @Override
    public DTOresult applyMethod(Message message) {
        Path filePath;

        var resDTO =  excelService.downloadGroupsToExcel();
        long chatId = message.getChatId();

        if (!resDTO.res()) {
            return resDTO;
        }

        filePath = (Path) resDTO.value();

        var strPath = filePath.toAbsolutePath().toString();
        InputFile document = new InputFile(new java.io.File(strPath));

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setCaption("Дерево групп в формате Excel");
        sendDocument.setDocument(document);

        return DTOresult.success(sendDocument);

    }
}
