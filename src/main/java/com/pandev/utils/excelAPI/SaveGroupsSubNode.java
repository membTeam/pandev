package com.pandev.utils.excelAPI;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOresult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j
public class SaveGroupsSubNode {

    private final GroupsRepository groupsRepo;

    @Transactional(propagation = Propagation.NESTED)
    public DTOresult saveGroupsSubNode(Groups subNode) {

        try {
            // Исключается дублирование txtgroup and parentNode
            if (groupsRepo.isExistsBytxtgroupAndParentnode(subNode.getTxtgroup(), subNode.getParentnode())) {
                throw new RuntimeException("Повторный ввод");
                //return new DTOresult(true, "exists", null);
            }

            /**
             * Позиция встраиваемого элемента в общей структуре дерева
             * относительно корневого элемента
             */
            int subMaxOrderNum = groupsRepo.maxOrdernum(subNode.getRootnode(), subNode.getParentnode());
            subNode.setOrdernum(++subMaxOrderNum);

            var subNodeSaved = groupsRepo.save(subNode);

            return DTOresult.success(subNodeSaved);

        } catch (RuntimeException ex) {
            log.error("saveSubNode: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("saveSubNode:" + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }

    }


}
