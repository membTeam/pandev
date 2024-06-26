package com.pandev.service;

import com.pandev.controller.ResponseHandler;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.InitListViewWithFormated;
import com.pandev.utils.MessageAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


/**
 * Класс вывод древовидной структуры в форматированном виде
 */
@Service(NotificationType.VIEW_TREE)
@RequiredArgsConstructor
public class ViewTree implements NotificationService {

    private final GroupsRepository groupsRepo;
    private final MessageAPI messageAPI;

    @Override
    public void applyMethod(Message mess) {

        var strFormated = InitListViewWithFormated.initViewFormated(groupsRepo);
        messageAPI.sendMessage(messageAPI.initMessage(mess.getChatId(), strFormated));
    }
}
