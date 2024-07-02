package com.pandev.service;

import com.pandev.controller.MessageAPI;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.motification.NotificationService;
import com.pandev.service.motification.NotificationType;
import com.pandev.utils.DTOparser;
import com.pandev.utils.DTOresult;
import com.pandev.utils.InitListViewWithFormated;
import com.pandev.utils.ParserMessage;
import com.pandev.utils.excelAPI.APIGroupsNode;
import com.pandev.utils.excelAPI.SaveGroupParentNode;
import com.pandev.utils.excelAPI.SaveGroupsSubNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


/**
 * Класс добавления элементов
 * Два обработчика: добавление корневого или дочернего элемента
 */
@Service(NotificationType.ADD_ELEMENT)
@RequiredArgsConstructor
@Log4j
public class AddElement implements NotificationService {

    private final GroupsRepository groupRepo;
    private final SaveGroupsSubNode saveGroupsSubNode;
    private final SaveGroupParentNode saveGroupParentNode;
    private final MessageAPI messageAPI;
    private final APIGroupsNode apiGroupsNode;


    /**
     * ДОбавление корневого элемента
     * Вся логика обработки в ExcelService.saveGroupParentFromExcel
     * @param chatId
     * @param arr
     * @return
     */
    private SendMessage addRootElement(long chatId, String[] arr) {

        var resultMes = MessageAPI.initMessage(chatId, null);
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
     * Вся логика обработки в ExcelService.saveDataByExcelToDb
     * @param chatId
     * @param arr массив 0 родительский элемент 1 субЭлемент
     * @return
     */
    private SendMessage addSubElement(long chatId, String[] arr) {
        var resultMessage = MessageAPI.initMessage(chatId, null);

        try {
            var parentNode = groupRepo.findByTxtgroup(arr[0].trim().toLowerCase());

            if (parentNode == null) {
                var strFormatedGroups = InitListViewWithFormated.initViewFormated(groupRepo);

                return MessageAPI.initMessage(chatId,
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


    public DTOresult applyMethodTest(long chatId, String[] arrParams) {
        SendMessage resultMes ;
        try {
            if (arrParams.length == 1) {
                resultMes = addRootElement(chatId, arrParams);
            } else {
                resultMes = addSubElement(chatId, arrParams);
            }

            return new DTOresult(true, resultMes.getText(), null);
        } catch (Exception ex) {
            return DTOresult.err(ex.getMessage());
        }

    }

    @Override
    public void applyMethod(Message mess) {

        DTOparser dtoParser = ParserMessage.getParsingMessage(mess);

        if (dtoParser.arrParams() == null || dtoParser.arrParams().length == 0) {
            messageAPI.sendMessage(MessageAPI.initMessage(mess.getChatId(),
                    "Формат команды должен включать:\n" +
                    "идентификатор команды и один или два аргумента\n"+
                    "Смотреть образец /help"));

            return;
        }

        try {
            if (dtoParser.arrParams().length == 1) {
                messageAPI.sendMessage(addRootElement(mess.getChatId(), dtoParser.arrParams()));
            } else {
                messageAPI.sendMessage(addSubElement(mess.getChatId(), dtoParser.arrParams()));
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            messageAPI.sendMessage(messageAPI.initMessage(mess.getChatId(), "Не известная ошибка добавления элемента"));
        }
    }
}
