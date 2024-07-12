package com.pandev.service.commands;


import com.pandev.controller.TelegramBot;
import com.pandev.dto.DTOresult;
import com.pandev.service.excelService.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.pandev.utils.Constants.*;
import com.pandev.controller.MessageAPI;
import com.pandev.service.strategyTempl.StrategyTempl;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Сервис команды /upload загрузка данных из Excel
 * используется специальный шаблон: any-data/extenal-resource/test-upload-excel.xlsx
 */
@Service
@RequiredArgsConstructor
public class Upload implements StrategyTempl {
    private final MessageAPI messageAPI;

    @Override
    public DTOresult applyMethod(Message message) {
        return messageAPI.infoMessageForUpload(message);
    }
}
