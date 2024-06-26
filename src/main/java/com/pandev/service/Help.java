package com.pandev.service;


import com.pandev.service.motification.NotificationService;
import com.pandev.service.motification.NotificationType;
import com.pandev.utils.Constants;
import com.pandev.utils.FileAPI;
import com.pandev.controller.MessageAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
