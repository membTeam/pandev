package com.pandev.commands;

import com.pandev.controller.MessageAPI;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.commands.RemoveElement;
import com.pandev.utils.ParserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RemoveElementTest {

    @Mock
    private GroupsRepository groupsRepo;

    @Mock
    private MessageAPI messageAPI;

    @Mock
    private Message message;

    @InjectMocks
    private RemoveElement removeElement;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void applyMethod() {
        var strText = "subelement";

        var sendMessage = SendMessage.builder()
                .chatId(1L)
                .text("empty")
                .build();

        var groups = Groups.builder()
                .id(1)
                .txtgroup(strText)
                .ordernum(1)
                .levelnum(1)
                .build();

        List<Groups> lsGroupForDelete = List.of(
                Groups.builder()
                        .id(2)
                        .parentnode(1)
                        .rootnode(1)
                        .levelnum(1)
                        .ordernum(1)
                        .build()
        );

        when(messageAPI.initMessage(any(long.class))).thenReturn(sendMessage);
        when(message.getChatId()).thenReturn(1L);
        when(message.getText()).thenReturn("/removeElement subelement");

        when(groupsRepo.findByTxtgroup(strText)).thenReturn(groups);
        when(groupsRepo.findByTxtgroup(strText)).thenReturn(groups);
        when(groupsRepo.findAllGroupsForUpdateOrdernum(any(int.class))).thenReturn(null);
        when(groupsRepo.selectGroupsForDelete(1)).thenReturn(lsGroupForDelete);

        doNothing().when(groupsRepo).deleteAll(any(List.class));
        doNothing().when(groupsRepo).deleteById(any(int.class));

        var res = removeElement.applyMethod(message);

        assertTrue(res.res());
    }
}
