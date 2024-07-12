package com.pandev.commands;


import com.pandev.controller.MessageAPI;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DownloadTest {

    @Mock
    private Message message;

    @InjectMocks
    private MessageAPI messageAPI;

    @Test
    public void applyMethod() {

    }

}
