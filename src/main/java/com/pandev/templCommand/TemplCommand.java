package com.pandev.templCommand;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Интерфейс шаблона Common
 */
public interface TemplCommand {
    SendMessage applyMethod(Message mess, CommService commServ);
}
