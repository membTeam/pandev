package com.pandev.utils;


import com.pandev.entities.TelegramChat;
import com.pandev.repositories.GroupsRepository;
import com.pandev.repositories.TelegramChatRepository;
import com.pandev.templCommand.CommCommand;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.HashMap;
import java.util.Map;

import static com.pandev.utils.Constants.*;

@Service
public class ResponseHandl {

    private final Map<Long, UserState> chatStates;
    private final CommCommand commCommand;
    private final FileAPI fileAPI;
    private final TelegramChatRepository telegramChatRepo;
    private SilentSender sender;
    private final GroupsRepository groupsRepository;


    public ResponseHandl(CommCommand commCommand, FileAPI fileAPI, TelegramChatRepository telegramChatRepo,
                         GroupsRepository groupsRepository) {
        this.commCommand = commCommand;
        this.fileAPI = fileAPI;
        this.telegramChatRepo = telegramChatRepo;

        chatStates = new HashMap<>();
        this.groupsRepository = groupsRepository;
    }

    @PostConstruct
    private void init() {
        commCommand.init(this);
    }

    public void init(SilentSender sender) {
        this.sender = sender;
    }

    private void replyToCancel(Message mess) {
        var message = initMessage(mess.getChatId(), null);

        try {
            message.setText(fileAPI.loadDataFromFile(FILE_HELP));
        } catch (Exception ex) {
            message.setText("Ошибка инициализации сообщения");
        }

        sender.execute(message);
    }

    /**
     * Обработка команд пользователя.
     * Не верный ввод команды обрабатывается в unexpectedCommand
     * @param message
     */
/*    private void replyToCommand(Message message) {
        switch (message.getText()) {
//            case COMD_REMOVE_ELEMENT, COMD_ADD_ELEMENT -> sendMessageToUser(message);
//            case COMD_HELP, COMD_VIEW_TREE -> useTemplCommand(message);
            case COMD_START -> sender.execute(
                    initMessage(message.getChatId(), "Вы уже в системе. Список команд: /help"));
            case COMD_CANCEL -> replyToCancel(message);

            default -> unexpectedCommand(message.getChatId());
        }
    }*/

    /**
     * Обработка сообщений на основе шаблона команд.
     * Сообщения, которые не предназначены для изменения БД
     * @param message
     */
/*    private void useTemplCommand(Message message) {
        SendMessage res = commCommand.initMessageFromStrCommand(message);
        sender.execute(res);
    }*/

    /**
     * На основе шаблона команд -> создание класса-обработчика для ответного сообщения.
     * Исключения перехватываются в методе initMessageFromStrCommand.
     * Эти классы изменяют состояние БД
     * @param message
     */
    /*private void addOrRemoveElement(Message message) {

        if (message.getText().equals(COMD_STOP)) {
            stopChat();
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
                commCommand.initMessageFromStrCommand(message) );

        chatStates.put(message.getChatId(), UserState.NONE);
    }*/

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
     * Не опознанная команда сообщения
     * @param chatId
     */
    private void unexpectedCommand(long chatId) {
        sender.execute(initMessage(chatId, "Команда не опознана."));
    }


    /**
     * Инициализация старта. Если пользователь не зарегистрирован, создается приглашение ввести имя.
     * Ответ пользователя обрабатывается в методе replyToName где будет регистрация в БД.
     * @param chatId
     */
    public void replyToStart(long chatId) {
        try {
            String file = FILE_START_REGISTER_USER;
            String text = fileAPI.loadDataFromFile(file);

            //TelegramChat telegramChat = telegramChatRepo.findByChatId(chatId);
/*            if (telegramChat == null) {
                TelegramChat.builder()
                        .chatId(chatId)
                        .build();
                groupsRepository.save(telegramChat);
            }*/

            SendMessage message = initMessage(chatId, text);
            sender.execute(message);

        } catch (Exception ex) {
            sender.execute(
                    initMessage(chatId, "Неизвестная ошибка. Зайдите позднее"));
        }

    }

    /**
     * Распределение входящих текстовых сообщений
     * @param update
     */
    public void replyToDistributionMess(Update update) {
        if (update != null && update.hasMessage()) {
            Message message = update.getMessage();

            var strCommand = ParserMessage.getstrCommandFromMessage(message);
            try {
                switch (strCommand) {
                    case COMD_START -> replyToStart(message.getChatId());
                    case COMD_ADD_ELEMENT, COMD_REMOVE_ELEMENT,
                         COMD_VIEW_TREE, COMD_HELP ->
                           sender.execute(commCommand.initMessageFromStrCommand(message));

                    case  COMD_STOP -> stopChat(message.getChatId());
                    default -> unexpectedCommand(message.getChatId());
                }
            } catch (Exception ex) {
                unexpectedCommand(message.getChatId());
            }
        }
    }

    public void initAndSendMessage(long chatId, String text) {
        var message = initMessage(chatId, text);
        sender.execute(message);
    }

    public boolean userIsActive(Long chatId) {
        return true;
                //chatStates.containsKey(chatId);
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
