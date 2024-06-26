package com.pandev.service;

import com.pandev.repositories.GroupsRepository;
import com.pandev.service.motification.NotificationService;
import com.pandev.service.motification.NotificationType;
import com.pandev.utils.InitListViewWithFormated;
import com.pandev.controller.MessageAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
