package com.pandev.commands;


import com.pandev.service.strategyTempl.FactoryService;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.when;

@SpringBootTest
public class DownloadTest02 {

    @Autowired
    private FactoryService commBeanService;

    @Mock
    private Message message;

    @Test
    private void responseToMessage() {



    }


}
