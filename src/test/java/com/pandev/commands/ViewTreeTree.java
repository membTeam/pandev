package com.pandev.commands;


import com.pandev.controller.MessageAPI;
import com.pandev.service.commands.ViewTree;
import com.pandev.utils.InitFormatedTreeService;
import com.pandev.utils.InitFormatedTreeString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ViewTreeTree {

    @Mock
    private InitFormatedTreeService initFormatedTreeService;

    @Mock
    private Message message;

    @Mock
    private MessageAPI messageAPI = Mockito.mock(MessageAPI.class);

    @InjectMocks
    private ViewTree viewTree;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void applyMethod() {

        when(initFormatedTreeService.getFormatedTreeString()).thenReturn("**resultTree");
        when(message.getChatId()).thenReturn(1L);

        var dtoRes = viewTree.applyMethod(message);

        assertTrue(dtoRes.res());

    }
}
