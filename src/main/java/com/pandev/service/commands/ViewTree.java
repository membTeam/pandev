package com.pandev.service.commands;


import com.pandev.dto.DTOresult;
import com.pandev.utils.InitFormatedTreeService;
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

    private final MessageAPI messageAPI;
    private final InitFormatedTreeService initFormatedTreeService;

    @Override
    public DTOresult applyMethod(Message mess) {

        var strFormated = initFormatedTreeService.getFormatedTreeString();
        if (strFormated.length() == 0) {
            return DTOresult.success(messageAPI.initMessage(mess.getChatId(), "В БД нет данных"));
        } else {
            return DTOresult.success(messageAPI.initMessage(mess.getChatId(), strFormated));
        }
    }
}
