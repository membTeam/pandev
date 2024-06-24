package com.pandev.templCommand;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOparser;
import com.pandev.utils.ParserMessage;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Класс удаление элемента по строковому идентификатору группы
 */
@NoArgsConstructor
public class ComdRemoveelement implements TemplCommand {

    private List<Groups> dataPreparation(Groups groups, CommService commServ) {

        var objMapInit = new Object(){
            public void put(Map<Integer, Groups> map, List<Groups> ls) {
                ls.forEach(item -> {
                    map.put(item.getId(), item);
                });
            }
        };

        Map<Integer, List<Groups>> mapTreeLevelnum = new TreeMap<>();
        Map<Integer, Groups> mapResult = new HashMap<>();

        var lsSelGroups = commServ.getGroupsRepo()
                            .selectGroupsForDelete(groups.getId());
        if (lsSelGroups.size() == 0) {
            return lsSelGroups;
        }

        var mapGroups = lsSelGroups.stream().collect(Collectors
                .groupingBy(Groups::getLevelnum));

        for (var item : mapGroups.entrySet()) {
            mapTreeLevelnum.put(item.getKey(), item.getValue());
        }

        for (var entrySet : mapTreeLevelnum.entrySet()) {
            var ls = entrySet.getValue();
            var parentNode = ls.get(0).getParentnode();

            if (mapResult.size() == 0 && parentNode.equals(groups.getId()) ) {
                objMapInit.put(mapResult, ls);
            } else if (mapResult.containsKey(parentNode)) {
                objMapInit.put(mapResult, ls);
            } else {
                break;
            }
        }

        return mapResult.values().stream().toList();
    }

    @Transactional
    @Override
    public SendMessage applyMethod(Message mess, CommService commServ) {

        DTOparser dtoParser = ParserMessage.getParsingMessage(mess);
        if (dtoParser.arrParams() == null || dtoParser.arrParams().length == 0) {
            return commServ.getResponseHandl().initMessage(mess.getChatId(),
                    "Формат команды должен включать:\n" +
                            "идентификатор команды и один аргумент\n"+
                            "Смотреть образец /help");
        }

        var groupRepo = commServ.getGroupsRepo();
        var result = commServ.getResponseHandl()
                .initMessage(mess.getChatId(), null);

        var strGroups = dtoParser.arrParams()[0].trim().toLowerCase();

        try {
            var currElement = groupRepo.findByTxtgroup(strGroups);
            if (currElement == null) {
                throw new IllegalArgumentException("Элемент не найден:" + strGroups);
            }

            if (currElement.getOrdernum() == 0) {
                groupRepo.deleteAll(
                        groupRepo.findAllElementByRootNode(currElement.getRootnode()) );

                result.setText("Выполнено ПОЛНОЕ удаление всех элементов корневого узла");
                return  result;
            }

            var groupsForDelete = dataPreparation(currElement, commServ);
            if (groupsForDelete.size() > 0) {
                groupRepo.deleteAll(groupsForDelete);
            }
            groupRepo.deleteById(currElement.getId());

            List<Groups> lsGroupsForUpdateOrdernum = groupRepo.findAllGroupsForUpdateOrdernum(currElement.getRootnode());
            if (lsGroupsForUpdateOrdernum.size() > 0) {
                var objOrderNum = new Object(){
                  public int ordernum = 1;
                };

                lsGroupsForUpdateOrdernum.forEach(item-> item.setOrdernum(objOrderNum.ordernum++));
                groupRepo.saveAll(lsGroupsForUpdateOrdernum);
            }

            result.setText("Выполнено удаление элемента:" + strGroups);

        } catch (Exception ex) {
            result.setText("Нет данных в БД :" + strGroups);
        }

        return result;

    }
}
