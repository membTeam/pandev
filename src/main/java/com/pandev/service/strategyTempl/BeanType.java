package com.pandev.service.strategyTempl;

import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Класс для поддержки определяемых значения
 */
public class BeanType {
    public static final String ADD_ELEMENT = "addElement";
    public static final String REMOVE_ELEMENT = "removeElement";
    public static final String HELP = "help";
    public static final String VIEW_TREE = "viewTree";
    public static final String DOWNLOAD = "download";
    public static final String UPLOAD = "upload";

    public static String getType(Message message) {
        var strMessage = message.hasDocument()
                ? "upload"
                : message.getText().substring(1).trim();

        var index = strMessage.indexOf(" ");
        String strCommand;
        if (index < 0) {
            strCommand = strMessage;
        } else {
            strCommand = strMessage.substring(0, index);
        }

        return switch (strCommand.trim().toUpperCase()) {
            case "ADDELEMENT" -> ADD_ELEMENT;
            case "REMOVEELEMENT" -> REMOVE_ELEMENT;
            case "VIEWTREE" -> VIEW_TREE;
            case "DOWNLOAD" -> DOWNLOAD;
            case "UPLOAD" -> UPLOAD;

            default -> HELP;
        };

    }
}
