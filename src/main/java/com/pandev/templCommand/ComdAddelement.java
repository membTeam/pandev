package com.pandev.templCommand;

import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOparser;
import com.pandev.utils.ParserMessage;
import com.pandev.utils.excelAPI.RecordDTOexcel;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import com.pandev.utils.DTOparser;


/**
 * Класс добавления элементов
 * Два обработчика: добавление корневого или дочернего элемента
 */
@NoArgsConstructor
public class ComdAddelement implements TemplCommand{

    private GroupsRepository groupRepo;

    /**
     * ДОбавление корневого элемента
     * Вся логика обработки в ExcelService.saveGroupParentFromExcel
     * @param chatId
     * @param arr
     * @param commServ интерфейс дополнительного функционала
     * @return
     */
    private SendMessage addRootElement(long chatId, String[] arr,  CommService commServ) {

        var result = commServ.getResponseHandl()
                .initMessage(chatId, null);

        var strGroup = arr[0].trim().toLowerCase();

        try {
            if (groupRepo.findByTxtgroup(strGroup) != null) {
                throw new IllegalArgumentException("Повторный ввод элемента:" + arr[0]);
            }

            var resSave = commServ.getExcelService().saveGroupParentFromExcel(strGroup);

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
     * @param commServ интерфейс дополнительного функционала
     * @return
     */
    private SendMessage addSubElement(long chatId, String[] arr, CommService commServ ) {

        var result = commServ.getResponseHandl()
                .initMessage(chatId, null);

        try {
            var parentNode = groupRepo.findByTxtgroup(arr[0].trim().toLowerCase());
            if (parentNode == null) {
                result.setText("Нет родительского элемента: " + arr[0]);
                return result;
            }

            var strSubNode = arr[1];

            var valParam = new RecordDTOexcel(parentNode.getTxtgroup(), strSubNode);
            var resSave = commServ.getExcelService().saveDataByExcelToDb(List.of(valParam));

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
    public SendMessage applyMethod(Message mess, CommService commServ) {

        DTOparser dtoParser = ParserMessage.getParsingMessage(mess);
        groupRepo = commServ.getGroupsRepo();

        try {
            if (dtoParser.arrParams().length == 1) {
                return addRootElement(mess.getChatId(), dtoParser.arrParams(), commServ);
            } else {
                return addSubElement(mess.getChatId(), dtoParser.arrParams(), commServ);
            }

        } catch (Exception ex) {
           return commServ.getResponseHandl()
                    .initMessage(mess.getChatId(), ex.getMessage());
        }
    }
}
