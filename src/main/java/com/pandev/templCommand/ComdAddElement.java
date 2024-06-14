package com.pandev.templCommand;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


/**
 * Класс добавление элемента обработка введенных данных пользователем
 */
@NoArgsConstructor
public class ComdAddElement implements TemplCommand{

    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {
        return commServ.getResponseHandl().initMessage(mess.getChatId(),
                "Метод добавление элемента не реализован");
    }
}
