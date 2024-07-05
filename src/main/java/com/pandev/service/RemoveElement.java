package com.pandev.service;

import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.pandev.repositories.GroupsRepository;
import com.pandev.controller.MessageAPI;
import com.pandev.service.strategyTempl.StrategyTempl;
import com.pandev.service.strategyTempl.BeanType;

import com.pandev.entities.Groups;
import com.pandev.dto.DTOparser;
import com.pandev.utils.ParserMessage;

/**
 * Класс удаление элемента по строковому идентификатору группы
 */
@Service(BeanType.REMOVE_ELEMENT)
@RequiredArgsConstructor
public class RemoveElement implements StrategyTempl {

    private final GroupsRepository groupsRepo;
    private final MessageAPI messageAPI;

    /**
     * Подготовка связанной структуры данных, которая будет удалена вместе с удаляемым элементом
     * @param groups
     * @return
     */
    private List<Groups> dataPreparation(Groups groups) {

        var objMapInit = new Object(){
            public void put(Map<Integer, Groups> map, List<Groups> ls) {
                ls.forEach(item -> {
                    map.put(item.getId(), item);
                });
            }
        };

        Map<Integer, List<Groups>> mapTreeLevelnum = new TreeMap<>();
        Map<Integer, Groups> mapResult = new HashMap<>();

        var lsSelectGroupsForDelete = groupsRepo.selectGroupsForDelete(groups.getId());
        if (lsSelectGroupsForDelete.size() == 0) {
            return lsSelectGroupsForDelete;
        }

        var mapGroupsByLevernum = lsSelectGroupsForDelete.stream().collect(Collectors
                .groupingBy(Groups::getLevelnum));

        for (var item : mapGroupsByLevernum.entrySet()) {
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

    @Override
    @Transactional
    public void applyMethod(Message mess) {

        DTOparser dtoParser = ParserMessage.getParsingMessage(mess);
        if (dtoParser.arrParams() == null || dtoParser.arrParams().length == 0) {
            messageAPI.sendMessage( messageAPI.initMessage(mess.getChatId(),
                    "Формат команды должен включать:\n" +
                            "идентификатор команды и один аргумент\n"+
                            "Смотреть образец /help"));
        }

        var result = MessageAPI.initMessage(mess.getChatId(), null);

        var strGroups = dtoParser.arrParams()[0].trim().toLowerCase();

        try {
            var currElement = groupsRepo.findByTxtgroup(strGroups);
            if (currElement == null) {
                throw new RuntimeException("Элемент не найден:" + strGroups);
            }

            if (currElement.getOrdernum() == 0) {
                groupsRepo.deleteAll(
                        groupsRepo.findAllElementByRootNode(currElement.getRootnode()) );

                result.setText("Выполнено ПОЛНОЕ удаление всех элементов корневого узла");
                messageAPI.sendMessage(result);
                return;
            }

            var groupsForDelete = dataPreparation(currElement);
            if (groupsForDelete.size() > 0) {
                groupsRepo.deleteAll(groupsForDelete);
            }
            groupsRepo.deleteById(currElement.getId());

            List<Groups> lsGroupsForUpdateOrdernum = groupsRepo.findAllGroupsForUpdateOrdernum(currElement.getRootnode());
            if (lsGroupsForUpdateOrdernum.size() > 0) {
                var objOrderNum = new Object(){
                  public int ordernum = 1;
                };

                lsGroupsForUpdateOrdernum.forEach(item-> item.setOrdernum(objOrderNum.ordernum++));
                groupsRepo.saveAll(lsGroupsForUpdateOrdernum);
            }

            result.setText("Выполнено удаление элемента:" + strGroups);

        } catch (Exception ex) {
            result.setText("Нет данных в БД :" + strGroups);
        }

        messageAPI.sendMessage(result);
    }
}
