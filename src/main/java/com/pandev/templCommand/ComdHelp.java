package com.pandev.templCommand;


import com.pandev.utils.Constants;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@NoArgsConstructor
public class ComdHelp implements TemplCommand {

    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {

        String text;

        var file = Constants.COMD_HELP;
        long chatId = mess.getChatId();

        try {
            text = commServ.getFileAPI().loadDataFromFile(file);
        } catch (Exception ex) {
            return commServ.getResponseHandl().initMessage(chatId, "Файл не найден");
        }

        return commServ.getResponseHandl().initMessage(chatId, text);
    }
}
