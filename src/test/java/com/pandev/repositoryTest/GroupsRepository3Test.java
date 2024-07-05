package com.pandev.repositoryTest;


import com.pandev.repositories.GroupsRepository;
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
    public void findAllGroupsForUpdateOrdernum_notExists () {
        var res = groupsRepo.findAllGroupsForUpdateOrdernum(10000);

        assertFalse(res.size()>0);
    }

    @Test
    public void findAllGroupsForDelete_notExists() {
        var res = groupsRepo.findAllGroupsForDelete(10000);

        assertFalse(res.size()>0);
    }

    @Test
    public void findAllGroupsForDelete_exists() {
        var res = groupsRepo.findAllGroupsForDelete(174);

        assertTrue(res.size()>0);
    }

}
