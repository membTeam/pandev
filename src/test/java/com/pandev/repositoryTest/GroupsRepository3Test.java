package com.pandev.repositoryTest;


import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.InitListGroups;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


/**
 * После пакетной загрузки, значения параметра/ов необходимо изменить
 */
@SpringBootTest
public class GroupsRepository3Test {

    @Autowired
    private GroupsRepository groupsRepo;



    @Test
    public void findAllGroupsForUpdateOrdernum_existsData () {
        var res = groupsRepo.findAllGroupsForUpdateOrdernum(171);

        assertTrue(res.size()>0);
    }

    @Test
    public void findAllGroupsForUpdateOrdernum_noData () {
        var res = groupsRepo.findAllGroupsForUpdateOrdernum(176);

        assertFalse(res.size()>0);
    }

    @Test
    public void findAllGroupsByParentIdExt_notExists() {
        var res = groupsRepo.findAllGroupsByParentIdExt(172);

        assertFalse(res.size()>0);

    }

    @Test
    public void findAllGroupsByParentIdExt() {
        var res = groupsRepo.findAllGroupsByParentIdExt(176);

        assertTrue(res.size()>0);

    }


    @Test
    public void convListObjToListGroups() {
        var res = groupsRepo.findAllRowsAfterCurrentStruct("управленцы", 171);

        var resConv = InitListGroups.convListObjToListGroups(res);

        assertTrue(resConv.size()>0);
    }

}
