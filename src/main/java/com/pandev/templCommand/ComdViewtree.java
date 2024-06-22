package com.pandev.templCommand;

import com.pandev.utils.InitListViewWithFormated;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


/**
 * Класс вывод древовидной структуры в форматированном виде
 */
@NoArgsConstructor
public class ComdViewtree implements TemplCommand  {

    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {

        var strFormated = InitListViewWithFormated.initViewFormated(commServ.getGroupsRepo());

        return commServ.getResponseHandl().initMessage(mess.getChatId(), strFormated);
    }
}
