package com.pandev.utils;


import com.pandev.controller.TelegramBot;
import com.pandev.entities.TelegramChat;
import com.pandev.repositories.GroupsRepository;
import com.pandev.repositories.TelegramChatRepository;
import jakarta.transaction.Transactional;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.HashMap;
import java.util.Map;

import static com.pandev.utils.Constants.*;

import com.pandev.templCommand.CommCommand;


public class ResponseHandl {

    private final SilentSender sender;
    private final GroupsRepository groupRepo;
    private final Map<Long, UserState> chatStates;
    private TelegramBot telegramBot;

    private CommCommand getCommonCommand() {
        return telegramBot.getCommCommand();
    }

    private String getUserName(long chatId) {

        var user = getChatRepository().findByChatId(chatId);
        if (user == null) {
            return "empty";
        }

        return user.getUserName();
    }

    private TelegramChatRepository getChatRepository() {
        return telegramBot.getTelegramChatRepo();
    }

    private FileAPI getFileApi() {
        return telegramBot.getFileAPI();
    }

    public void init(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public ResponseHandl(SilentSender sender, DBContext db, GroupsRepository repo) {
        this.sender = sender;
        //this.chatStates = db.getMap(Constants.CHAT_STATES);

        chatStates = new HashMap<>();

        this.groupRepo = repo;
    }

    /**
     * Источник вызова метода: replyToDistributionMess
     * @param message
     */
    private void replyToMess(Message message) {

        if (message.getText().equalsIgnoreCase( COMD_STOP)) {
            stopChat(message.getChatId());
        }

        switch (chatStates.get(message.getChatId())) {
            case AWAITING_NAME -> replyToName(message);
            case AWAITING_ADDELEMENT,
                 AWAITING_REMOVEELEMENT -> addOrRemoveElement(message);
            default -> replyToCommand(message);
        }
    }

    private void replyToCommand(Message message) {
        switch (message.getText()) {
            case COMD_REMOVE_ELEMENT, COMD_ADD_ELEMENT -> sendMessageToUser(message);
            case COMD_HELP, COMD_VIEW_TREE -> useTemplCommand(message);
            case COMD_START -> sender.execute(initMessage(message.getChatId(), "Вы уже в системе. Список команд: /help"));
            default -> unexpectedMessage(message.getChatId());
        }
    }

    /**
     * Использование шаблона команд на основе рефлексии
     * @param message
     */
    private void useTemplCommand(Message message) {
        SendMessage res = getCommonCommand().initMessageFromStrCommand(message, message.getText());
        sender.execute(res);
    }

    private void addOrRemoveElement(Message message) {

        String strCommand;
        if (chatStates.get(message.getChatId()).equals(UserState.AWAITING_ADDELEMENT)) {
            strCommand = COMD_ADD_ELEMENT;
        } else if (chatStates.get(message.getChatId()).equals(UserState.AWAITING_REMOVEELEMENT)) {
            strCommand = COMD_REMOVE_ELEMENT;
        } else {
            var text = "Не верный формат команды.\nДля списка команд используйте /help";
            var respMessage = initMessage(message.getChatId(), text);

            sender.execute(respMessage);
            return;
        }

        sender.execute(getCommonCommand().initMessageFromStrCommand(message, strCommand));
        chatStates.put(message.getChatId(), UserState.NONE);
    }

    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Спасибо, что использовали наш TelegramBot\nНажмите /start чтобы повторить общение.");
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));

        sender.execute(sendMessage);
    }

    @Transactional
    private void replyToName(Message message) {
        var chatId = message.getChatId();
        SendMessage respMessage = new SendMessage();
        respMessage.setChatId(chatId);

        try {
            var user = TelegramChat.builder()
                    .chatId(chatId)
                    .userName(message.getText())
                    .build();

            getChatRepository().save(user);

            chatStates.put(chatId, UserState.NONE);

            var text = getFileApi().loadDataFromFile(FILE_START_REGISTER_USER);
            respMessage.setText(text);

        } catch (Exception ex) {
            respMessage.setText("Не известная ошибка. Войдите позднее");
        }

        sender.execute(respMessage);
    }

    /**
     * Не опознанная команда сообщения
     * @param chatId
     */
    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Команда не опознана.");
        sender.execute(sendMessage);
    }

    /**
     * Изменение ствтуса, отправить сообщение из файла и изменить статуса
     * используется только для "/removeElement", "/addElement"
     * @param message
     */
    private void sendMessageToUser(Message message) {
        var chatId = message.getChatId();

        var text = message.getText();
        String textFromFile;

        var file = switch (text) {
            case COMD_REMOVE_ELEMENT -> {
                chatStates.put(chatId, UserState.AWAITING_REMOVEELEMENT);
                yield Constants.FILE_REMOVEELEMENT;
            }
            case COMD_ADD_ELEMENT -> {
                chatStates.put(chatId, UserState.AWAITING_ADDELEMENT);
                yield Constants.FILE_ADDELEMENT;}
            default -> Constants.FILE_EMPTY;
        };

        try {
            textFromFile = getFileApi().loadDataFromFile(file);
        } catch (Exception ex) {
            chatStates.put(chatId, UserState.NONE);
            unexpectedMessage(chatId);
            return;
        }

        sender.execute(initMessage(chatId, textFromFile));

    }

    /**
     * Инициализация страта
     * @param chatId
     */
    public void replyToStart(long chatId) {

        String file;

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        try {
            TelegramChat telegramChat = getChatRepository().findByChatId(chatId);

            if (telegramChat == null) {
                file = FILE_START_NOT_REGISTER_USER;
                chatStates.put(chatId, UserState.AWAITING_NAME);
            } else {
                file = FILE_START_REGISTER_USER;
                chatStates.put(chatId, UserState.NONE);
            }

            message.setText(getFileApi().loadDataFromFile(file));

        } catch (Exception ex) {
            message.setText("Неизвестная ошибка. Зайдите позднее");
        }

        sender.execute(message);
    }

    /**
     * Распределение входящих сообщений
     * @param update
     */
    public void replyToDistributionMess(Update update) {
     if (update != null && update.hasMessage() && update.getMessage().hasText()) {
            replyToMess(update.getMessage());
        }
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }

    /**
     * Создание шаблона текстового сообщения по умолчанию
     * @param chatId
     * @param mes
     * @return
     */
    public SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes)
                .build();
    }

}
