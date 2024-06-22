package com.pandev.repositoryTest;


import com.pandev.repositories.GroupsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupsRepositoryTest {

    @Autowired
    private GroupsRepository groupsRepo;


    @Test
    public void findAllGroups() {
        var res = groupsRepo.findAllGroups();
        assertTrue(res.size()>0);
    }

    @Test
    public void findAllGroupsByParentId() {
        var res = groupsRepo.findAllGroupsByParentId(19, 19);

        assertFalse(res.size()>0);
    }


    @Test
    public void isExistsBytxtgroupAndParentnode() {
        var res = groupsRepo.isExistsBytxtgroupAndParentnode("управленцы", 19);

        assertTrue(res);
    }

    @Test
    public void findAllByParentnodeIn() {

        List<Integer> ls = List.of(7);
        var res = groupsRepo.findAllByParentnodeInAndOrdernumNot(ls,0);

        assertTrue(res.size()==0);

    }

    @Test
    public void findAllByTxtgroupIn() {
        List<String> ls = List.of("java","Управленцы" );

        var res = groupsRepo.findAllByTxtgroupIn(ls);

        assertTrue(res.size()>0);


    }

    @Test
    public void maxOrdernum() {
        var res = groupsRepo.maxOrdernum(178, 184);

        assertNotNull(res);
    }


    @Test
    public void findAllElementByRoorNode() {
        var res = groupsRepo.findAllElementByRootNode(100);
        assertTrue(res.size()>0);
    }

    @Test
    public void findByOrdernum() {
        var res = groupsRepo.findListGroupsByOrdernum(100, 6);

        assertTrue(res.size()>0);
    }


    @Test
    public void findByTxtgroup() {
        var firstEl = groupsRepo.firstElement();
        var res = groupsRepo.findByTxtgroup(firstEl.getTxtgroup());

        assertNotNull(res);
    }


}
