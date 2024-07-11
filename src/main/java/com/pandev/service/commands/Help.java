package com.pandev.service.commands;


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
    public void applyMethod(Message mess) {


        String text;

        var file = Constants.FILE_HELP;
        long chatId = mess.getChatId();

        try {
            text = fileAPI.loadTxtDataFromFile(file);

            var mes = messageAPI.initMessage(chatId, text);
            messageAPI.sendMessage(mes);

        } catch (Exception ex) {
            var mes = messageAPI.initMessage(chatId, "Файл не найден");
            messageAPI.sendMessage(mes);
        }
    }
}
