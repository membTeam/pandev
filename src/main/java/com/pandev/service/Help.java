package com.pandev.service;


import com.pandev.controller.ResponseHandler;
import com.pandev.utils.Constants;
import com.pandev.utils.FileAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


@Service(NotificationType.HELP)
@RequiredArgsConstructor
public class Help implements NotificationService {
    private final FileAPI fileAPI;
    private final ResponseHandler responseHandler;

    @Override
    public SendMessage applyMethod(Message mess) {

        String text;

        var file = Constants.FILE_HELP;
        long chatId = mess.getChatId();

        try {
            text = fileAPI.loadDataFromFile(file);

            return responseHandler.initMessage(chatId, text);
        } catch (Exception ex) {
            return responseHandler.initMessage(chatId, "Файл не найден");
        }
    }
}
