package com.pandev.utils;

import com.pandev.dto.GroupsDetails;
import com.pandev.repositories.GroupsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class InitFormatedTreeStringTest {

    @Mock
    private GroupsRepository groupsRepo;

    @Test
    public void getFormatedTreeString() {
        List<GroupsDetails> lsGroups = List.of(
            new GroupsDetails(1, "groups1")
        );

        groupsRepo = Mockito.mock(GroupsRepository.class);
        when(groupsRepo.getTreeData()).thenReturn(lsGroups);

        var res = InitFormatedTreeString.getFormatedTreeString(groupsRepo);

        assertTrue(res.length()>0);

    }
}
