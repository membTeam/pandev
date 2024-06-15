package com.pandev.repositoryTest;


import com.pandev.repositories.TelegramChatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hibernate.validator.internal.util.Contracts.*;

@SpringBootTest
public class TelegramChatTest {

    @Autowired
    private TelegramChatRepository telegramChatRepo;

    @Test
    public void verifyRepository() {

    }

}
