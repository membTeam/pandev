package com.pandev.utils;

import com.pandev.dto.DTOparser;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Парсинг объекта Message. Выделение из Message структуры данных: chatId, strCommand and String[] strParams
 */
public class ParserMessage {

    /**
     * Выделение из Message субСтроки команды
     * @param message
     * @return
     */
    public static String getstrCommandFromMessage(Message message) {
        var text = message.getText();
        var indexSpace = text.indexOf(" ");
        if (indexSpace < 0) {
            return text;
        } else {
            return text.substring(0, indexSpace);
        }
    }

    /**
     * Парсинг Message. Выделение структуры данных:
     * chatId, strCommand, String[] параметров.
     * @param message
     * @return
     */
    public static DTOparser getParsingMessage(Message message) {

        long chatId = message.getChatId();
        String strCommand;
        String[] arrParams;

        var text = message.getText();
        var indexSpace = text.indexOf(" ");

        if (indexSpace < 0) {
            strCommand = text;
            arrParams = null;
        } else {
            strCommand = text.substring(0, indexSpace);
            var strParams = text.substring(indexSpace).trim();
            var indexSeparator = strParams.indexOf(" ");

            if (indexSeparator < 0) {
                arrParams = new String[]{strParams};
            } else {
                arrParams = new String[]{strParams.substring(0, indexSeparator),
                        strParams.substring(indexSeparator).trim() };
            }
        }

        return new DTOparser(chatId, strCommand, arrParams);
    }

}
