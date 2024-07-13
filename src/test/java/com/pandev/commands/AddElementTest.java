package com.pandev.commands;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.pandev.controller.MessageAPI;
import com.pandev.dto.DTOresult;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.commands.AddElement;
import com.pandev.service.excelService.APIGroupsNode;
import com.pandev.service.excelService.ServiceParentNode;
import com.pandev.service.excelService.ServiceSubNode;


public class AddElementTest {

    @Mock
    private Message message;

    @Mock
    private MessageAPI messageAPI;

    @Mock
    private GroupsRepository groupsRepo;

    @Mock
    private ServiceParentNode saveGroupParentNode;

    @Mock
    private ServiceSubNode saveGroupsSubNode;

    @Mock
    private APIGroupsNode getGroupsNode;

    @Mock
    private APIGroupsNode apiGroupsNode;

    @InjectMocks
    private AddElement addElement;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void applyMethod_addSubElement() {
        var strParentNode = "parentnode";
        var strSubNode = "subelement";

        var sendMessage = SendMessage.builder()
                .chatId(1L)
                .text("empty")
                .build();

        var parentGroups = Groups.builder()
                .id(1)
                .txtgroup(strParentNode)
                .build();

        var subGroups = Groups.builder()
                .id(2)
                .build();

        var dtoSave = DTOresult.success(subGroups);

        when(message.getChatId()).thenReturn(1L);
        when(message.getText()).thenReturn("/addElement " + strParentNode + " " + strSubNode);

        when(messageAPI.initMessage(any(long.class))).thenReturn(sendMessage);

        when(groupsRepo.findByTxtgroup(strParentNode)).thenReturn(parentGroups);
        when(groupsRepo.findByTxtgroup(strSubNode)).thenReturn(null);

        when(apiGroupsNode.initGroups(any(String.class), any(Groups.class))).thenReturn(subGroups);

        when(saveGroupsSubNode.saveSubNode(subGroups)).thenReturn(dtoSave);

        var res = addElement.applyMethod(message);

        assertTrue(res.res());
    }

    @Test
    public void applyMethod_addRootElement() {
        var strParentNode = "parentNode";

        var sendMessage = SendMessage.builder()
                .chatId(1L)
                .text("empty")
                .build();

        var parentSave = Groups.builder()
                .id(1)
                .parentnode(0)
                .rootnode(0)
                .levelnum(0)
                .ordernum(1)
                .txtgroup(strParentNode)
                .build();

        var dtoParentNode = DTOresult.success(parentSave);

        when(message.getChatId()).thenReturn(1L);
        when(message.getText()).thenReturn("/addElement " + strParentNode);

        when(messageAPI.initMessage(any(long.class))).thenReturn(sendMessage);
        when(getGroupsNode.getGroups(strParentNode)).thenReturn(null);
        when(groupsRepo.findByTxtgroup(any(String.class))).thenReturn(null);

        when(saveGroupParentNode.saveParentNode(any(String.class))).thenReturn(dtoParentNode);

        var res = addElement.applyMethod(message);

        assertTrue(res.res());

    }

}
