package com.pandev.service;

import com.pandev.controller.ResponseHandler;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.InitListViewWithFormated;
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
    private final ResponseHandler responseHandler;

    @Override
    public SendMessage applyMethod(Message mess) {

        var strFormated = InitListViewWithFormated.initViewFormated(groupsRepo);

        return responseHandler.initMessage(mess.getChatId(), strFormated);
    }
}
