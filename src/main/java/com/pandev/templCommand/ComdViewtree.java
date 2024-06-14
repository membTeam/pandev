package com.pandev.templCommand;

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

        var lsGroups = commServ.getGroupsRepo().getTreeData().stream()
                .map(item-> "_".repeat(item.levelnum()) + item.txtgroup())
                .toArray();

        var sb = new StringBuffer();
        for (var item : lsGroups) {
            sb.append(item + "\n");
        }

        return commServ.getResponseHandl().initMessage(mess.getChatId(),
                sb.toString());
    }
}
