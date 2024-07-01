package com.pandev.utils.excelAPI;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOresult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveGroupsSubNode {

    private final GroupsRepository groupsRepo;
    private final GetGroupsNode getGroupsNode;

    @Transactional
    public DTOresult saveGroupsSubNode(Groups subNode) {
        var subFromRepo = getGroupsNode.getGroups(subNode.getTxtgroup());

        if (subFromRepo != null) {
            return new DTOresult(true, "ok", subFromRepo);
        }

        /**
         * Позиция встраиваемого элемента в общей структуре дерева
         * относительно корневого элемента
         */
        int subMaxOrderNum = getGroupsNode.getMaxOrderNum(subNode);
                //groupsRepo.maxOrdernum(subNode.getRootnode(), subNode.getParentnode());
        subNode.setOrdernum(++subMaxOrderNum);

        subFromRepo = groupsRepo.save(subNode);

        return DTOresult.success(subFromRepo);

    }


}
