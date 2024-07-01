package com.pandev.utils.excelAPI;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetGroupsNode {

    private final GroupsRepository groupsRepo;

    @Transactional(readOnly = true)
    public boolean isExistsGroupNode(String txtGroups) {
        return groupsRepo.findByTxtgroup(txtGroups.trim().toLowerCase()) != null;
    }


    @Transactional(readOnly = true)
    public Groups getGroups(String txtGroups) {
        return groupsRepo.findByTxtgroup(txtGroups);
    }

    @Transactional(readOnly = true)
    public Integer getMaxOrderNum(Groups subNode) {
        return groupsRepo.maxOrdernum(subNode.getRootnode(), subNode.getParentnode());
    }

}
