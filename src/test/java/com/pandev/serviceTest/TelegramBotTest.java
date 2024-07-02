package com.pandev.serviceTest;


import com.pandev.service.AddElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TelegramBotTest {

    @Autowired
    private AddElement addElement;

    @Test
    public void applyMethodTest() {
        long chatId = 1L;
        String[] arr = {"parNode", "subNode"};

        var res = addElement.applyMethodTest(chatId, arr);

        assertTrue(res.res());

    }

}
