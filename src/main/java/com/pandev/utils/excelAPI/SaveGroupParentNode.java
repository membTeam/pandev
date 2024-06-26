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
public class SaveGroupParentNode {

    private  final GroupsRepository groupsRepo;
    private final GetGroupsNode getGroupsNode;

    /**
     * Запись в поле txtgroup в строчных символах,
     * а при отображении в telegramBoт д/быть преобразование первого символа в прописной
     * @param
     * @return
     */
    @Transactional
    public DTOresult saveGroupParentFromExcel(String strRootnode) {

        strRootnode = strRootnode.trim().toLowerCase();

        /**
         * Если есть такой узел в БД -> return groupsFromRepo
         */
        var groupsFromRepo = getGroupsNode.getGroups(strRootnode);
        if (groupsFromRepo != null) {
            return DTOresult.success(groupsFromRepo);
        }

        Groups groups = Groups.builder()
                .rootnode(-1)
                .parentnode(-1)
                .ordernum(0)
                .levelnum(0)
                .txtgroup(strRootnode)
                .build();


        var resSave = groupsRepo.save(groups);

        resSave.setParentnode(resSave.getId());
        resSave.setRootnode(resSave.getId());

        var finalSaved = groupsRepo.save(resSave);

        return DTOresult.success(finalSaved);

    }
}
