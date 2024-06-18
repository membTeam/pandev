package com.pandev.templCommand;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.InitListGroups;
import com.pandev.utils.excelAPI.RecordDTOexcel;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;


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
     * @param message
     * @param arr
     * @param commServ интерфейс дополнительного функционала
     * @return
     */
    @Transactional
    private SendMessage addRootElement(Message message, String[] arr,  CommService commServ) {

        var result = commServ.getResponseHandl()
                .initMessage(message.getChatId(), null);

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
     * @param message
     * @param arr массив 0 родительский элемент 1 субЭлемент
     * @param commServ интерфейс дополнительного функционала
     * @return
     */
    @Transactional
    private SendMessage addSubElement(Message message, String[] arr, CommService commServ ) {

        var result = commServ.getResponseHandl()
                .initMessage(message.getChatId(), null);

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

        groupRepo = commServ.getGroupsRepo();

        var arrParams = mess.getText().split(" ");
        if (arrParams.length > 2) {
            return commServ.getResponseHandl()
                    .initMessage(mess.getChatId(), "Ошибка параметров");
        }

        if (arrParams.length == 1) {
            return addRootElement(mess, arrParams, commServ);
        } else {
            return addSubElement(mess, arrParams, commServ);
        }

    }
}
