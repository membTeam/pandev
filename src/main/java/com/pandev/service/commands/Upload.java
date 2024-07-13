package com.pandev.service.commands;


import com.pandev.dto.DTOresult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.pandev.controller.MessageAPI;
import com.pandev.service.strategyTempl.StrategyTempl;

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
        return messageAPI.replyToUpload(message);
    }
}
