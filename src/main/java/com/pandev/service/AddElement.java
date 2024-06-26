package com.pandev.service;

import com.pandev.controller.ResponseHandler;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOparser;
import com.pandev.utils.InitListViewWithFormated;
import com.pandev.utils.MessageAPI;
import com.pandev.utils.ParserMessage;
import com.pandev.utils.excelAPI.ExcelService;
import com.pandev.utils.excelAPI.RecordDTOexcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;


/**
 * Класс добавления элементов
 * Два обработчика: добавление корневого или дочернего элемента
 */
@Service(NotificationType.ADD_ELEMENT)
@RequiredArgsConstructor
@Log4j
public class AddElement implements NotificationService {

    private final GroupsRepository groupRepo;
    private final ExcelService excelService;
    private final MessageAPI messageAPI;

    /**
     * ДОбавление корневого элемента
     * Вся логика обработки в ExcelService.saveGroupParentFromExcel
     * @param chatId
     * @param arr
     * @return
     */
    private SendMessage addRootElement(long chatId, String[] arr) {

        var result = MessageAPI.initMessage(chatId, null);

        var strGroup = arr[0].trim().toLowerCase();

        try {
            if (groupRepo.findByTxtgroup(strGroup) != null) {
                throw new IllegalArgumentException("Повторный ввод элемента:" + arr[0]);
            }

            var resSave = excelService.saveGroupParentFromExcel(strGroup);

            if (!resSave.res()) {
                throw new IllegalArgumentException(resSave.value().toString());
            }

            result.setText("Создан корневой элемент: " + arr[0]);

        } catch (Exception ex) {
            result.setText("Неизвестная ошибка записи в БД");
        }

        return result;
    }

    /**
     * Добавление субЭлемента только если есть родительский элемент.
     * Вся логика обработки в ExcelService.saveDataByExcelToDb
     * @param chatId
     * @param arr массив 0 родительский элемент 1 субЭлемент
     * @return
     */
    private SendMessage addSubElement(long chatId, String[] arr) {

        var result = MessageAPI.initMessage(chatId, null);

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

            var strSubNode = arr[1];

            var valParam = new RecordDTOexcel(parentNode.getTxtgroup(), strSubNode);
            var resSave = excelService.saveDataByExcelToDb(List.of(valParam));

            if (!resSave.res()) {
                throw new IllegalArgumentException(resSave.value().toString());
            }

            result.setText("Добавлена дочерняя группа: " + strSubNode);

        } catch (Exception ex) {
            result.setText("Не известная ошибка записи в БД");
        }

        return result;
    }

    @Override
    public void applyMethod(Message mess) {

        DTOparser dtoParser = ParserMessage.getParsingMessage(mess);

        if (dtoParser.arrParams() == null || dtoParser.arrParams().length == 0) {
            messageAPI.sendMessage(MessageAPI.initMessage(mess.getChatId(),
                    "Формат команды должен включать:\n" +
                    "идентификатор команды и один или два аргумента\n"+
                    "Смотреть образец /help"));
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
