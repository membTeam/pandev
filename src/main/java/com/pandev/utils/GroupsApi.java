package com.pandev.utils;


import com.pandev.controller.TelegramBot;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Используетсмя только для сохранения корневого узла
 */
@Service
public class GroupsApi {
    private GroupsRepository groupRepo;

    public GroupsApi(GroupsRepository groupRepo) {
        this.groupRepo = groupRepo;
    }

    @Transactional
    public DTOresult saveRootNode(String strRoot) {
        try {
            Groups groups = Groups.builder()
                    .rootnode(-1)
                    .parentnode(-1)
                    .ordernum(0)
                    .levelnum(0)
                    .txtgroup(strRoot)
                    .build();
            var groupSave = groupRepo.save(groups);

            groupSave.setRootnode(groupSave.getId());
            groupSave.setParentnode(groupSave.getId());

            groupRepo.save(groupSave);

            return new DTOresult(true, groupSave);

        } catch (Exception ex) {
            return new DTOresult(false, "Ошибка записи в БД");
        }
    }

    @Transactional
    public DTOresult saveSubNode(Groups parentNode, String strParent) {
        try {
            if (parentNode == null) {
                throw new IllegalArgumentException("parentNode is null");
            }

            int subOrderNum = groupRepo.maxOrdernum(parentNode.getRootnode(), parentNode.getId());

            Groups groups = Groups.builder()
                    .rootnode(parentNode.getRootnode())
                    .parentnode(parentNode.getId())
                    .ordernum(++subOrderNum)
                    .levelnum(parentNode.getLevelnum() + 1)
                    .txtgroup(strParent)
                    .build();

            var lsObjForMoved = groupRepo.findAllGroupsBytxtGroup(parentNode.getTxtgroup(), parentNode.getRootnode());
            if (lsObjForMoved.size() > 0) {
                List<Groups> lsGroups = InitListGroups.convListObjToListGroups(lsObjForMoved);
                lsGroups.forEach(item-> item.setOrdernum(item.getOrdernum() + 1));
                groupRepo.saveAll(lsGroups);
            }

            var resSave = groupRepo.save(groups);

            return new DTOresult(true, resSave);

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }
    }

}
