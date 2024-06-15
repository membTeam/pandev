package com.pandev.templCommand;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.InitListGroups;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;


/**
 * Класс добавления элементов
 * Два обработчика: добавление корневого или дочернего элементов
 */
@NoArgsConstructor
public class ComdAddelement implements TemplCommand{

    private GroupsRepository groupRepo;

    /**
     * ДОбавление корневого элемента
     * @param message
     * @param arr
     * @param commServ интерфейс дополнительного функционала
     * @return
     */
    @Transactional
    private SendMessage addRootElement(Message message, String[] arr,  CommService commServ) {

        var result = commServ.getResponseHandl()
                .initMessage(message.getChatId(), null);

        if ( groupRepo.findByTxtgroup(arr[0]) != null) {
            result.setText("Повторный ввод элемента:" + arr[0]);
            return result;
        }

        try {
            Groups groups = Groups.builder()
                    .parentnode(-1)
                    .rootnode(-1)
                    .ordernum(0)
                    .levelnum(0)
                    .txtgroup(arr[0])
                    .build();
            var groupSave = groupRepo.save(groups);

            groupSave.setRootnode(groupSave.getId());
            groupSave.setParentnode(groupSave.getId());

            groupRepo.save(groupSave);

            result.setText("Создан корневой элемент: " + groupSave.getTxtgroup());

        } catch (Exception ex) {
            result.setText("Неизвестная ошибка записи в БД");
        }

        return result;
    }

    /**
     * Добавление субЭлемента только если есть родительский элемент.
     * Коллекция lsGroups используется для переопределения поля ordernum.
     * В итоге ВСЕ подчиненные элементы смещаются в структуре дерева относительно rootElement.
     * Уровень встраиваемого элемента в структуре дерева определяется полем levelnum.
     * @param message
     * @param arr массив 0 родительский элемент 1 субЭлемент
     * @param commServ интерфейс дополнительного функционала
     * @return
     */
    @Transactional
    private SendMessage addSubElement(Message message, String[] arr, CommService commServ ) {

        var result = commServ.getResponseHandl()
                .initMessage(message.getChatId(), null);

        // Родительский элемент
        var parentNode = groupRepo.findByTxtgroup(arr[0]);
        if (parentNode == null) {
            result.setText("Главный элемент не найден:" + arr[0]);
            return result;
        }

        try {
            // Массив элементов для смещения в структуре дерева
            // относительно родительского узла
            List<Groups> lsGroups = null;
            var lsObjForMoved = groupRepo.findAllGroupsBytxtGroup(parentNode.getTxtgroup());
            if (lsObjForMoved.size() > 0) {
                lsGroups = InitListGroups.convListObjToListGroups(lsObjForMoved);
            }

            // Позиция встраиваемого элемента в общей структуре дерева
            // относительно родительского элемента
            int subOrderNum = lsGroups == null
                    ? parentNode.getOrdernum() + 1
                    : lsGroups.stream().mapToInt(item -> item.getOrdernum()).min().orElseThrow();

            // Изменение позиции элементов в структуре дерева
            // и сохранение в БД
            if (lsGroups != null) {
                lsGroups.forEach(item-> item.setOrdernum(item.getOrdernum() + 1));
                groupRepo.saveAll(lsGroups);
            }

            // Встраиваемый элемент
            Groups groups = Groups.builder()
                    .rootnode(parentNode.getRootnode())
                    .parentnode(parentNode.getId())
                    .ordernum(subOrderNum)
                    .levelnum(parentNode.getLevelnum() + 1)
                    .txtgroup(arr[1])
                    .build();

            groupRepo.save(groups);

            result.setText("Добавлена дочерняя группа: " + arr[1]);

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
