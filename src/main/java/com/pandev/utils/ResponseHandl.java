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

    public ResponseHandl(SilentSender sender, DBContext db, GroupsRepository repo) {
        this.sender = sender;
        //this.chatStates = db.getMap(Constants.CHAT_STATES);

        chatStates = new HashMap<>();

        this.groupRepo = repo;
    }

    /**
     * Инициализация TelegramBot, который используется как источник для bean objects
     * @param telegramBot
     */
    public void init(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

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

    private void replyToCancel(Message mess) {
        var message = initMessage(mess.getChatId(), null);

        try {
            message.setText(getFileApi().loadDataFromFile(FILE_HELP));
        } catch (Exception ex) {
            message.setText("Ошибка инициализации сообщения");
        }

        sender.execute(message);
    }

    /**
     * Обработка на основе текущего состояния из chatStates
     * Состояние NONE используется для обработки введенных команд
     * Остальные состояния для обработки ответа на приглашение ввода параметра
     * @param message
     */
    private void replyToMess(Message message) {

        if (message.getText().equalsIgnoreCase( COMD_STOP)) {
            stopChat(message.getChatId());
        }

        switch (chatStates.get(message.getChatId())) {
            case AWAITING_NAME -> replyToName(message);
            case AWAITING_ADD_ELEMENT,
                 AWAITING_REMOVE_ELEMENT -> addOrRemoveElement(message);
            default -> replyToCommand(message);
        }
    }

    /**
     * Обработка команд пользователя.
     * Не верный ввод команды обрабатывается в unexpectedCommand
     * @param message
     */
    private void replyToCommand(Message message) {
        switch (message.getText()) {
            case COMD_REMOVE_ELEMENT, COMD_ADD_ELEMENT -> sendMessageToUser(message);
            case COMD_HELP, COMD_VIEW_TREE -> useTemplCommand(message);
            case COMD_START -> sender.execute(
                    initMessage(message.getChatId(), "Вы уже в системе. Список команд: /help"));
            case COMD_CANCEL -> replyToCancel(message);

            default -> unexpectedCommand(message.getChatId());
        }
    }

    /**
     * Обработка сообщений на основе шаблона команд.
     * Сообщения, которые не предназначены для изменения БД
     * @param message
     */
    private void useTemplCommand(Message message) {
        SendMessage res = getCommonCommand().initMessageFromStrCommand(message, message.getText());
        sender.execute(res);
    }

    /**
     * На основе шаблона команд -> создание класса-обработчика для ответного сообщения.
     * Исключения перехватываются в методе initMessageFromStrCommand.
     * Эти классы изменяют состояние БД
     * @param message
     */
    private void addOrRemoveElement(Message message) {

        if (message.getText().equals(COMD_CANCEL)) {
            replyToCancel(message);
            return;
        }

        String strCommand;
        var chatStateUser = chatStates.get(message.getChatId());

        if (chatStateUser.equals(UserState.AWAITING_ADD_ELEMENT)) {
            strCommand = COMD_ADD_ELEMENT;
        } else if (chatStateUser.equals(UserState.AWAITING_REMOVE_ELEMENT)) {
            strCommand = COMD_REMOVE_ELEMENT;
        } else {
            var text = "Не верный формат команды.\nДля списка команд используйте /help";
            var respMessage = initMessage(message.getChatId(), text);

            chatStates.put(message.getChatId(), UserState.NONE);
            sender.execute(respMessage);

            return;
        }

        sender.execute(
                    getCommonCommand().initMessageFromStrCommand(message, strCommand) );

        chatStates.put(message.getChatId(), UserState.NONE);
    }

    /**
     * Остановка telegramBot и закрытие ВСЕХ ресурсов.
     * Повторный вход только через кнопку start или команду /start
     * @param chatId
     */
    private void stopChat(long chatId) {
        SendMessage sendMessage = initMessage(chatId,
                "Спасибо, что использовали наш TelegramBot" +
                "\nНажмите /start чтобы повторить общение.");

        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));

        sender.execute(sendMessage);
    }

    /**
     * Регистрация нового пользователя в БД. Из message извлекается имя пользователя.
     * В заключении устанавливается состояние NONE.
     * Это позволит вводить команды в telegramBot
     * @param message
     */
    @Transactional
    private void replyToName(Message message) {
        var chatId = message.getChatId();
        SendMessage respMessage = initMessage(chatId, null);

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
    private void unexpectedCommand(long chatId) {
        sender.execute(initMessage(chatId, "Команда не опознана."));
    }

    /**
     * Отправить сообщение из файла и изменить статус пользователя.
     * Используется только для команд: /removeElement, /addElement
     * @param message
     */
    private void sendMessageToUser(Message message) {
        var chatId = message.getChatId();
        String textFromFile;

        var file = switch (message.getText()) {
            case COMD_REMOVE_ELEMENT -> {
                chatStates.put(chatId, UserState.AWAITING_REMOVE_ELEMENT);
                yield FILE_REMOVE_ELEMENT;
            }
            case COMD_ADD_ELEMENT -> {
                chatStates.put(chatId, UserState.AWAITING_ADD_ELEMENT);
                yield FILE_ADD_ELEMENT;}
            default -> FILE_DEFAULT;
        };

        try {
            textFromFile = getFileApi().loadDataFromFile(file);
        } catch (Exception ex) {
            chatStates.put(chatId, UserState.NONE);
            unexpectedCommand(chatId);
            return;
        }

        sender.execute(initMessage(chatId, textFromFile));
    }

    /**
     * Инициализация старта. Если пользователь не зарегистрирован, создается приглашение ввести имя.
     * Ответ пользователя обрабатывается в методе replyToName где будет регистрация в БД.
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
        if (update != null && update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                replyToMess(message);
            } else if (message.hasDocument()) {
                replyToDocument(message);
            }
        }
    }

    private void replyToDocument(Message message) {
        try {
            var resFile = telegramBot.downloadDocument(message);

            sender.execute(
                    initMessage(message.getChatId(), "Файл загружен")  );

        } catch (Exception ex) {
            sender.execute(
                    initMessage(message.getChatId(), "Не известная ошибка загрузки документа") );
        }
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }

    /**
     * Создание шаблона текстового сообщения.
     * @param chatId
     * @param mes
     * @return
     */
    public SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes == null ? "empty" : mes)
                .build();
    }

}
