package com.pandev.templCommand;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class ComdDownload implements TemplCommand{

    private SendMessage initExcelDocument(Message mess, CommService commServ) {

        var resDTO = commServ.getExcelService().writeGridsToExcel();

        var result = commServ.getResponseHandl().initMessage(mess.getChatId(),
                resDTO.value().toString());
    }

    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {

        return initExcelDocument(mess, commServ);
    }
}
