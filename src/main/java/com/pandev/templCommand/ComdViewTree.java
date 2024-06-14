package com.pandev.templCommand;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


/**
 * Класс вывод древовидной структуры в форматированном виде
 */
@NoArgsConstructor
public class ComdViewTree implements TemplCommand  {

    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {
        return commServ.getResponseHandl().initMessage(mess.getChatId(),
                "Метод ViewTree не реализован");
    }
}
