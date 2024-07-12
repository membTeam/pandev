package com.pandev.service;

import com.pandev.entities.Groups;
import com.pandev.service.excelService.APIGroupsNode;
import com.pandev.service.excelService.ServiceParentNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServiceParentNodeTest {

    @Mock
    private APIGroupsNode getGroupsNode;

    @InjectMocks
    private ServiceParentNode serviceParentNode;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void saveParentNode() {

        var txtGroups = "anyText";

        var parentNode = Groups.builder()
                .id(1)
                .parentnode(1)
                .rootnode(1)
                .txtgroup(txtGroups)
                .build();

        when(getGroupsNode.getGroups(any(String.class))).thenReturn(parentNode);

        var resSave = serviceParentNode.saveParentNode(txtGroups);

        assertTrue(resSave.res());
    }

}
