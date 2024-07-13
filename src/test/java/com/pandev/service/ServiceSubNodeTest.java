package com.pandev.service;

import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.excelService.APIGroupsNode;
import com.pandev.service.excelService.ServiceSubNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServiceSubNodeTest {

    @Mock
    GroupsRepository groupsRepo;

    @Mock
    private APIGroupsNode getGroupsNode;

    @InjectMocks
    private ServiceSubNode serviceSubNode;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void saveSubNode_notExists() {
        var txtGroups = "SubAnyText";

        var parentNode = Groups.builder()
                .id(1)
                .parentnode(1)
                .rootnode(1)
                .txtgroup("parentNode")
                .build();

        var subNode = Groups.builder()
                .id(2)
                .parentnode(1)
                .rootnode(1)
                .txtgroup(txtGroups)
                .build();

        when(groupsRepo.save(any(Groups.class))).thenReturn(subNode);
        when(getGroupsNode.getGroups(any(String.class))).thenReturn(null);

        var res = serviceSubNode.saveSubNode(parentNode);

        assertTrue(res.res());
    }

    @Test
    public void saveSubNode() {

        var txtGroups = "SubAnyText";

        var parentNode = Groups.builder()
                .id(1)
                .parentnode(1)
                .rootnode(1)
                .txtgroup("parentNode")
                .build();

        var subNode = Groups.builder()
                .id(2)
                .parentnode(1)
                .rootnode(1)
                .txtgroup(txtGroups)
                .build();

        when(getGroupsNode.getGroups(any(String.class))).thenReturn(subNode);

        var res = serviceSubNode.saveSubNode(parentNode);

        assertTrue(res.res());

    }



}
