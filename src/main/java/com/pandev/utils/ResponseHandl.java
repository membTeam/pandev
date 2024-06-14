package com.pandev.utils;


import com.pandev.controller.TelegramBot;
import com.pandev.repositories.GroupsRepository;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.HashMap;
import java.util.Map;

import static com.pandev.utils.Constants.START_TEXT;

import com.pandev.templCommand.CommCommand;


public class ResponseHandl {

    private final SilentSender sender;
    private final GroupsRepository groupRepo;
    private final Map<Long, UserState> chatStates;
    private TelegramBot telegramBot;


    private CommCommand getCommCommand() {
        return telegramBot.getCommCommand();
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

        var chatId = message.getChatId();

        if (message.getText().equalsIgnoreCase("/stop")) {
            stopChat(chatId);
        }

        switch (chatStates.get(chatId)) {
            case AWAITING_NAME -> replyToName(message);
            case AWAITING_COMMAND -> replyToCommand(message); //  replyToCommand(message);
            default -> unexpectedMessage(chatId);
        }
    }

    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Спасибо, что использовали наш TelegramBot\nНажмите /start чтобы повторить общение.");
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
    }

    private void replyToName(Message message) {
        var chatId = message.getChatId();

        promptWithKeyboardForState(chatId, "Привет " + message.getText() +
                        ". Для получения списка команд /help",
                UserState.AWAITING_COMMAND);
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
    private void replyMessageToUser(Message message) {
        var chatId = message.getChatId();

        var text = message.getText();
        String textFromFile;

        var file = switch (text) {
            case "/removeElement" -> {
                chatStates.put(chatId, UserState.AWAITING_REMOVEELEMENT);
                yield Constants.COMD_REMOVEELEMENT;
            }
            case "/addElement" -> {
                chatStates.put(chatId, UserState.AWAITING_ADDELEMENT);
                yield Constants.COMD_ADDELEMENT;}
            default -> Constants.COMD_EMPTY;
        };

        try {
            textFromFile = getFileApi().loadDataFromFile(file);
        } catch (Exception ex) {
            chatStates.put(chatId, UserState.AWAITING_COMMAND);
            unexpectedMessage(chatId);
            return;
        }

        sender.execute(initMessage(chatId, textFromFile));

    }

    public void replyToCommand(Message message) {

        // Ответное сообщение от пользователя
        if (message.getText().charAt(0) != '/') {
                /*
                (chatStates.get(message.getChatId()).equals(UserState.AWAITING_ADDELEMENT)
                    || chatStates.get(message.getChatId()).equals(UserState.AWAITING_REMOVEELEMENT))) {
                 */
            if (chatStates.get(message.getChatId()).equals(UserState.AWAITING_ADDELEMENT)) {
                message.setText("/addElement " +  message.getText());
            }

            if (chatStates.get(message.getChatId()).equals(UserState.AWAITING_REMOVEELEMENT)) {
                message.setText("/removeElement " + message.getText());
            } else {
                chatStates.put(message.getChatId(), UserState.AWAITING_COMMAND);
                unexpectedMessage(message.getChatId());
                return;
            }

            var res = getCommCommand().preparationClass(message);

        }

        switch (message.getText()) {
            case "/removeElement", "/addElement" -> replyMessageToUser(message);
            case "/help" -> sender.execute(getCommCommand().preparationClass(message));
            case "/viewTree" -> sender.execute(getCommCommand().preparationClass(message));
            default -> unexpectedMessage(message.getChatId());
        }
    }

    /**
     * Инициализация страта
     * @param chatId
     */
    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);

        chatStates.put(chatId, UserState.AWAITING_NAME);
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

    public void promptWithKeyboardForState(long chatId, String text,
                                            UserState awaitingState) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sender.execute(sendMessage);

        if (awaitingState != UserState.NONE) {
            chatStates.put(chatId, awaitingState);
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
