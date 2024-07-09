package com.pandev.service.commands;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.pandev.repositories.GroupsRepository;
import com.pandev.service.strategyTempl.StrategyTempl;
import com.pandev.service.strategyTempl.BeanType;
import com.pandev.utils.InitFormatedTreeString;
import com.pandev.controller.MessageAPI;


/**
 * Класс вывод древовидной структуры в форматированном виде
 */
@Service(BeanType.VIEW_TREE)
@RequiredArgsConstructor
public class ViewTree implements StrategyTempl {

    private final GroupsRepository groupsRepo;
    private final MessageAPI messageAPI;

    @Override
    public void applyMethod(Message mess) {

        var strFormated = InitFormatedTreeString.getFormatedTreeString(groupsRepo);
        messageAPI.sendMessage(messageAPI.initMessage(mess.getChatId(), strFormated));
    }
}
