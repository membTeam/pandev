package com.pandev.repositoryTest;


import com.pandev.repositories.GroupsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupsRepositoryTest {

    @Autowired
    private GroupsRepository groupsRepo;


    @Test
    public void findAllElementByRoorNode() {
        var res = groupsRepo.findAllElementByRoorNode(100);
        assertTrue(res.size()>0);
    }

    @Test
    public void findByOrdernum() {
        var res = groupsRepo.findListGroupsByOrdernum(100, 6);

        assertTrue(res.size()>0);
    }

    @Test
    public void findAllGroupsBytxtGroup() {
        var res = groupsRepo.findAllGroupsBytxtGroup("SecondElement130");

        assertTrue(res.size()>0);
    }

    @Test
    public void findByTxtgroup() {
        var firstEl = groupsRepo.firstElement();
        var res = groupsRepo.findByTxtgroup(firstEl.getTxtgroup());

        assertNotNull(res);
    }


}
