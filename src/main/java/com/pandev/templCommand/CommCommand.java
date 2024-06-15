package com.pandev.templCommand;

import com.pandev.utils.ResponseHandl;
import com.pandev.controller.TelegramBot;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.FileAPI;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.lang.reflect.Constructor;


/**
 * Класс предназначен для создания объекта из Message.getText()
 * Объект создается на основе методов рефлексии
 */
@Service
public class CommCommand implements CommService {

    private final GroupsRepository groupsRepo;
    private final FileAPI fileAPI;
    private TelegramBot telegramBot;

    public CommCommand(GroupsRepository groupsRepo, FileAPI fileAPI) {
        this.groupsRepo = groupsRepo;
        this.fileAPI = fileAPI;
    }

    /**
     * Дополнительная инициализация из TelegramBot
     * @param telegramBot
     */
    public void init(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }


    private static String lowercaseFirstLetter(String word) {
        if (word.charAt(0) == '/') {
            word = word.substring(1);
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    /**
     * Создание объекта на основе методов рефлекции
     * @param message ответное сообщение пользователя
     * @param strCommand строковый идентификатор команды
     * @return
     */
    public SendMessage initMessageFromStrCommand(Message message, String strCommand) {

        var strFormCommand = String.format("Comd%s", lowercaseFirstLetter(strCommand));
        var pathClass = String.format("%s.%s",
                this.getClass().getPackageName(), strFormCommand);

        try {
            Class clazz = Class.forName(pathClass);
            Constructor<?> constructor = clazz.getConstructor();
            TemplCommand obj = (TemplCommand) constructor.newInstance();

            return obj.applyMethod(message, this);

        } catch (Exception e) {
            return getResponseHandl().initMessage(message.getChatId(),"внутренняя ошибка.");
        }
    }


    @Override
    public GroupsRepository getGroupsRepo() {
        return groupsRepo;
    }

    @Override
    public ResponseHandl getResponseHandl() {
        return telegramBot.getResponseHandl();
    }

    @Override
    public FileAPI getFileAPI() {
        return fileAPI;
    }
}
