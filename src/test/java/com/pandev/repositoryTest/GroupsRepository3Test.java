package com.pandev.repositoryTest;


import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.InitListGroups;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupsRepository3Test {

    @Autowired
    private GroupsRepository groupsRepo;


    @Test
    public void convListObjToListGroups_notData() {
        var res = groupsRepo.findAllGroupsBytxtGroup("SecondElement1300");

        var resConv = InitListGroups.convListObjToListGroups(res);

        assertTrue(resConv.size()==0);
    }


    @Test
    public void convListObjToListGroups() {
        var res = groupsRepo.findAllGroupsBytxtGroup("SecondElement130");

        var resConv = InitListGroups.convListObjToListGroups(res);

        assertTrue(resConv.size()>0);
    }

}
