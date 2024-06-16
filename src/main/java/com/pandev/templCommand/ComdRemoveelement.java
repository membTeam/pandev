package com.pandev.templCommand;

import com.pandev.entities.Groups;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

/**
 * Класс удаление элемента по строковому идентификатору группы
 */
@NoArgsConstructor
public class ComdRemoveelement implements TemplCommand {

    @Transactional
    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {

        var groupRepo = commServ.getGroupsRepo();
        var result = commServ.getResponseHandl()
                .initMessage(mess.getChatId(), null);

        var strGroups = mess.getText();

        try {
            var currElement = groupRepo.findByTxtgroup(strGroups);
            if (currElement == null) {
                result.setText("Элемент не найден:" + strGroups);
                return result;
            }

            if (currElement.getOrdernum() == 0) {
                groupRepo.deleteAll(
                        groupRepo.findAllElementByRootNode(currElement.getRootnode()) );

                result.setText("Выполнено ПОЛНОЕ удаление всех элементов корневого узла");
                return  result;
            }

            List<Groups> lsGroups = groupRepo.findListGroupsByOrdernum(currElement.getRootnode(), currElement.getOrdernum());

            groupRepo.deleteById(currElement.getId());

            // Изменение позиции элементов в структуре дерева
            // и сохранение в БД
            if (lsGroups.size() > 0) {

                lsGroups.forEach(item-> item.setOrdernum(item.getOrdernum() - 1));
                groupRepo.saveAll(lsGroups);
            }

            result.setText("Выполнено удаление элемента:" + strGroups);

        } catch (Exception ex) {
            result.setText("Не известная ошибка удаления элемента");
        }

        return result;

    }
}
