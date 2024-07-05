package com.pandev.utils.excelAPI;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.dto.DTOresult;

@Service
@RequiredArgsConstructor
public class ServiceSubNode {

    private final GroupsRepository groupsRepo;
    private final APIGroupsNode getGroupsNode;

    @Transactional
    public DTOresult saveSubNode(Groups subNode) {
        var subFromRepo = getGroupsNode.getGroups(subNode.getTxtgroup());

        if (subFromRepo != null) {
            return new DTOresult(true, "ok", subFromRepo);
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
