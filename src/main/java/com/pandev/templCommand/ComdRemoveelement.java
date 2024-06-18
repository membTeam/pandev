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

        var strGroups = mess.getText().trim().toLowerCase();

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

            Integer rootNode = currElement.getRootnode();
            List<Groups> lsGroupsForDelete = groupRepo.findAllGroupsByParentIdExt(currElement.getId());
            if (lsGroupsForDelete.size() > 0) {
                groupRepo.deleteAll(lsGroupsForDelete);
            }

            groupRepo.deleteById(currElement.getId());

            List<Groups> lsGroupsForUpdateOrderNum = groupRepo.findAllGroupsForUpdateOrdernum(rootNode);
            if (lsGroupsForUpdateOrderNum.size() > 0) {
                var objAccount = new Object(){
                  public int ordernum = 1;
                };
                lsGroupsForUpdateOrderNum.forEach(item-> item.setOrdernum(objAccount.ordernum++));
                groupRepo.saveAll(lsGroupsForUpdateOrderNum);
            }

            result.setText("Выполнено удаление элемента:" + strGroups);

        } catch (Exception ex) {
            result.setText("Не известная ошибка удаления элемента");
        }

        return result;

    }
}
