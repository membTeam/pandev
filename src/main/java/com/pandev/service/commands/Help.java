package com.pandev.service.commands;


import com.pandev.dto.DTOresult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.pandev.service.strategyTempl.StrategyTempl;
import com.pandev.service.strategyTempl.BeanType;
import com.pandev.utils.Constants;
import com.pandev.utils.FileAPI;
import com.pandev.controller.MessageAPI;


@Service(BeanType.HELP)
@RequiredArgsConstructor
public class Help implements StrategyTempl {
    private final FileAPI fileAPI;
    private final MessageAPI messageAPI;

    @Override
    public DTOresult applyMethod(Message mess) {

        String text;

        var file = Constants.FILE_HELP;
        long chatId = mess.getChatId();

        try {
            text = fileAPI.loadTxtDataFromFile(file);

            return DTOresult.success(messageAPI.initMessage(chatId, text));

        } catch (Exception ex) {
            return DTOresult.err("Файл не найден");
        }
    }
}
