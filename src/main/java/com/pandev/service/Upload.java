package com.pandev.service;


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
    public void applyMethod(Message message) {

        messageAPI.uploadDocument(message);

    }
}
