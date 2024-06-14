package com.pandev.templCommand;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Класс удаление элемента. Обработка данных введенных пользователем
 */
@NoArgsConstructor
public class ComdRemoveElement implements TemplCommand {

    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {
        return commServ.getResponseHandl().initMessage(mess.getChatId(),
                "Метод удаление элемента не реализован");
    }
}
