package com.pandev.service.commands;

import com.pandev.controller.MessageAPI;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.strategyTempl.StrategyTempl;
import com.pandev.service.strategyTempl.BeanType;
import com.pandev.dto.DTOparser;
import com.pandev.dto.DTOresult;
import com.pandev.utils.InitFormatedTreeString;
import com.pandev.utils.ParserMessage;
import com.pandev.service.excelService.APIGroupsNode;
import com.pandev.service.excelService.ServiceParentNode;
import com.pandev.service.excelService.ServiceSubNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


/**
 * Класс добавления элементов
 * Два обработчика: добавление корневого или дочернего элемента
 */
@Service(BeanType.ADD_ELEMENT)
@RequiredArgsConstructor
@Log4j
public class AddElement implements StrategyTempl {

    private final GroupsRepository groupRepo;
    private final ServiceSubNode saveGroupsSubNode;
    private final ServiceParentNode saveGroupParentNode;
    private final MessageAPI messageAPI;
    private final APIGroupsNode apiGroupsNode;


    /**
     * Добавление корневого элемента.
     * @param chatId
     * @param arr
     * @return
     */
    private SendMessage addRootElement(long chatId, String[] arr) {

        var resultMes = messageAPI.initMessage(chatId);
        var strGroupParent = arr[0].trim().toLowerCase();

        try {
            if (groupRepo.findByTxtgroup(strGroupParent) != null) {
                resultMes.setText("Повторный ввод элемента");
                return resultMes;
            }

            var resSaved = (Groups) saveGroupParentNode.saveParentNode(strGroupParent).value();
            resultMes.setText("Создан корневой элемент: " + resSaved.getTxtgroup());

        } catch (Exception ex) {
            resultMes.setText("Неизвестная ошибка записи в БД");
        }

        return resultMes;
    }

    /**
     * Добавление субЭлемента только если есть родительский элемент.
     * @param chatId
     * @param arr массив 0 родительский элемент 1 субЭлемент
     * @return
     */
    private SendMessage addSubElement(long chatId, String[] arr) {
        var resultMessage = messageAPI.initMessage(chatId);

        try {
            var parentNode = groupRepo.findByTxtgroup(arr[0].trim().toLowerCase());

            if (parentNode == null) {
                var strFormatedGroups = InitFormatedTreeString.getFormatedTreeString(groupRepo);

                return messageAPI.initMessage(chatId,
                        "Корневой узел не найден.\n" +
                        "Сверьте свои данные с деревом групп.\n" +
                        "--------------------\n" +
                        strFormatedGroups);
            }

            var strSubNode = arr[1].trim().toLowerCase();

            if (groupRepo.findByTxtgroup(strSubNode) != null) {
                resultMessage.setText("Повторный ввод элемента");
                return resultMessage;
            }

            var groups = apiGroupsNode.initGroups(strSubNode, parentNode);
            var resSaved = (Groups) saveGroupsSubNode.saveSubNode(groups).value();

            resultMessage.setText("Добавлена дочерняя группа: " + resSaved.getTxtgroup());

        } catch (Exception ex) {
            resultMessage.setText("Не известная ошибка записи в БД");
        }

        return resultMessage;
    }


    @Override
    public DTOresult applyMethod(Message mess) {

        DTOparser dtoParser = ParserMessage.getParsingMessage(mess);

        if (dtoParser.arrParams() == null || dtoParser.arrParams().length == 0) {
            return DTOresult.err("Формат команды должен включать:\n" +
                    "идентификатор команды и один или два аргумента\n"+
                    "Смотреть образец /help");
        }

        try {
            if (dtoParser.arrParams().length == 1) {
                 return DTOresult.success(addRootElement(mess.getChatId(), dtoParser.arrParams()));
            } else {
                return DTOresult.success(addSubElement(mess.getChatId(), dtoParser.arrParams()));
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            return DTOresult.success(messageAPI.initMessage(mess.getChatId(), "Не известная ошибка добавления элемента"));
        }
    }
}
