package com.pandev.service;


import com.pandev.controller.ResponseHandler;
import com.pandev.utils.Constants;
import com.pandev.utils.FileAPI;
import com.pandev.utils.MessageAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


@Service(NotificationType.HELP)
@RequiredArgsConstructor
public class Help implements NotificationService {
    private final FileAPI fileAPI;
    private final MessageAPI messageAPI;

    @Override
    public void applyMethod(Message mess) {


        String text;

        var file = Constants.FILE_HELP;
        long chatId = mess.getChatId();

        try {
            text = fileAPI.loadDataFromFile(file);

            var mes = messageAPI.initMessage(chatId, text);
            messageAPI.sendMessage(mes);

        } catch (Exception ex) {
            var mes = messageAPI.initMessage(chatId, "Файл не найден");
            messageAPI.sendMessage(mes);
        }
    }
}
