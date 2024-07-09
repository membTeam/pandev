package com.pandev.service.excelService;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Вспомогательный сервис, используемый при обработке
 * добавления, удаления элементов из таблицы Groups,
 * а также в качестве вспомогательного API
 */
@Service
@RequiredArgsConstructor
public class APIGroupsNode {

    private final GroupsRepository groupsRepo;

    /**
     * Инициализация объекта Groups.
     * Используется при добавлении rootNode and subNode
     * @param txtSubNode
     * @param groupsParent
     * @return
     */
    public Groups initGroups(String txtSubNode, Groups groupsParent) {

        Groups subGroups = Groups.builder()
                .rootnode(groupsParent.getRootnode())
                .parentnode(groupsParent.getId())
                .levelnum(groupsParent.getLevelnum() + 1)
                .ordernum(0)    // назначается в saveSubNodeFromExcel
                .txtgroup(txtSubNode.trim().toLowerCase())
                .build();

        return  subGroups;
    }

    /**
     * Возвращает элемент по текстовому идентификатору из БД.
     * Используется для проверки перед добавлением rootNode or subNode.
     * Значение поля txtGroups уникально на уровне таблицы
     * @param txtGroups
     * @return
     */
    @Transactional(readOnly = true)
    public Groups getGroups(String txtGroups) {
        return groupsRepo.findByTxtgroup(txtGroups);
    }

    /**
     * Максимальное значение ordernum в контексте rootNode.
     * Это значение используется для пересчета по всем добавляемым элементам.
     * ordernum - индекс последовательности элементов по отношению к rootNode
     * @param subNode
     * @return
     */
    @Transactional(readOnly = true)
    public Integer getMaxOrderNum(Groups subNode) {
        return groupsRepo.maxOrdernum(subNode.getRootnode(), subNode.getParentnode());
    }

}
