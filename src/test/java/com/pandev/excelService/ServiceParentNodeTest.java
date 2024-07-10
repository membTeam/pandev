package com.pandev.excelService;


import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.excelService.ServiceParentNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
public class ServiceParentNodeTest {

    @Autowired
    private ServiceParentNode serviceParentNode;

    @MockBean
    private GroupsRepository groupsRepo;


    @Test
    public void saveParentNode_withExistsParent() {
        var txtGroups = "any text";

        var parentGroups = Groups.builder()
                .id(10)
                .parentnode(10)
                .rootnode(10)
                .txtgroup(txtGroups)
                .levelnum(0)
                .ordernum(0)
                .build();

        when(groupsRepo.findByTxtgroup(any(String.class))).thenReturn(parentGroups);

        var res = serviceParentNode.saveParentNode(txtGroups);

        assertTrue(res.res());
        assertEquals(10, ((Groups)res.value()).getId());

    }

    @Test
    public void saveParentNode_NoExistsParent() {
        var txtGroups = "anyTextGroups";

        var parentGroups = Groups.builder()
                .id(10)
                .parentnode(10)
                .rootnode(10)
                .txtgroup(txtGroups)
                .levelnum(0)
                .ordernum(0)
                .build();

        when(groupsRepo.save(any(Groups.class))).thenReturn(parentGroups);

        var res = serviceParentNode.saveParentNode(txtGroups);

        assertTrue(res.res());
        assertEquals(10, ((Groups)res.value()).getId());

    }
}
