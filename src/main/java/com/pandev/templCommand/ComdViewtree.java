package com.pandev.templCommand;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


/**
 * Класс вывод древовидной структуры в форматированном виде
 */
@NoArgsConstructor
public class ComdViewtree implements TemplCommand  {

    private String convChar(String str) {
        var result = str.trim().toUpperCase();
        result = result.substring(0,1) + result.substring(1).toLowerCase();

        return result;
    }

    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {

        var lsGroups = commServ.getGroupsRepo().getTreeData().stream()
                .map(item-> "*".repeat(item.levelnum()) + convChar(item.txtgroup()))
                .toArray();

        var sb = new StringBuffer();
        for (var item : lsGroups) {
            sb.append(item + "\n");
        }

        return commServ.getResponseHandl().initMessage(mess.getChatId(),
                sb.toString());
    }
}
