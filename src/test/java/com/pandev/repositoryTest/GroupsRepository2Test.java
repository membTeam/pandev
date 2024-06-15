package com.pandev.repositoryTest;


import com.pandev.repositories.GroupsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class GroupsRepository2Test {

    @Autowired
    private GroupsRepository groupsRepo;


    @Test
    public void findFirsElement() {
        var res = groupsRepo.firstElement();

        assertNotNull(res);
    }

    @Test
    public void getTreeData() {
        var res = groupsRepo.getTreeData();

        assertTrue(res.size()>0);
    }

}
