package com.pandev.service.excelService;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.dto.DTOresult;


/**
 * Вспомогательный API для обработки subNode.
 * Используется при обработке команд /addElement and /upload (from Excel)
 */
@Service
@RequiredArgsConstructor
public class ServiceSubNode {

    private final GroupsRepository groupsRepo;
    private final APIGroupsNode getGroupsNode;

    @Transactional
    public DTOresult saveSubNode(Groups subNode) {
        var subFromRepo = getGroupsNode.getGroups(subNode.getTxtgroup());

        if (subFromRepo != null) {
            return DTOresult.success(subFromRepo);
        }

        /**
         * Позиция встраиваемого элемента в общей структуре дерева
         * относительно корневого элемента
         */
        int subMaxOrderNum = getGroupsNode.getMaxOrderNum(subNode);

        subNode.setOrdernum(++subMaxOrderNum);

        subFromRepo = groupsRepo.save(subNode);

        return DTOresult.success(subFromRepo);

    }

}
