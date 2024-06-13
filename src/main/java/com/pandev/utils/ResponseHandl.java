package com.pandev.utils;


import com.pandev.controller.TelegramBot;
import com.pandev.repositories.GroupsRepository;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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

    public void init(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public ResponseHandl(SilentSender sender, DBContext db, GroupsRepository repo) {
        this.sender = sender;
        //this.chatStates = db.getMap(Constants.CHAT_STATES);

        chatStates = new HashMap<>();

        this.groupRepo = repo;
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);

        chatStates.put(chatId, UserState.AWAITING_NAME);
    }

    public void replyToButtons(long chatId, Message message) {
        if (message.getText().equalsIgnoreCase("/stop")) {
            stopChat(chatId);
        }

        switch (chatStates.get(chatId)) {
            case AWAITING_NAME -> replyToName(chatId, message);
            case AWAITING_COMMAND -> replyToCommand(chatId, message);
            default -> unexpectedMessage(chatId);
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

    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Спасибо, что использовали наш TelegramBot\nНажмите /start чтобы повторить общение.");
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
    }

    private void replyToName(long chatId, Message message) {
        promptWithKeyboardForState(chatId, "Привет " + message.getText() +
                        ". Для получения списка команд /help",
                UserState.AWAITING_COMMAND);
    }

    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Команда не опознана.");
        sender.execute(sendMessage);
    }

    public void replyToCommand(long chatId, Message message) {

        var comand = switch (message.getText()) {
            case "/addElement" -> "addElement";
            case "/viewTree" -> "viewTree";
            case "/removeElement" -> "removeElement";
            case "/help" -> "help";
            default -> "empty";
        };

        if (comand.equals("empty")) {
            promptWithKeyboardForState(chatId,
                    "Команда " + message.getText()+ " не опознана",
                    UserState.NONE );
            return;
        }

        if (comand.equals("help")) {
            var res = getCommCommand().preparationClass(message);
            sender.execute(res);
            return ;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message.getText() + " не реализована.");

        sender.execute(sendMessage);
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }

    public SendMessage initMessage(long chatId, String mes) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(mes)
                .build();
    }

}
