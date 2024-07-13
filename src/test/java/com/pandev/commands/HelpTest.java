package com.pandev.commands;


import com.pandev.controller.MessageAPI;
import com.pandev.dto.DTOresult;
import org.junit.jupiter.api.BeforeEach;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.pandev.service.commands.Help;
import com.pandev.utils.FileAPI;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class HelpTest {

    @Mock
    private FileAPI fileAPI;

    @Mock
    private Message message;

    @Mock
    private MessageAPI messageAPI;

    @InjectMocks
    private Help help;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void applyMethod() throws IOException {

        var text = "dataFromFile";
        var sendMessage = SendMessage.builder()
                .chatId(1L)
                .text("dataFromFile")
                .build();

        when(message.getChatId()).thenReturn(1L);
        when(fileAPI.loadTxtDataFromFile(any(String.class))).thenReturn(text);
        when(messageAPI.initMessage(1L, text)).thenReturn(sendMessage);

        var res = help.applyMethod(message);

        assertTrue(res.res());
    }

}
