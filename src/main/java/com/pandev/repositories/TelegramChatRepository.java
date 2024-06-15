package com.pandev.repositories;

import com.pandev.entities.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChatRepository extends JpaRepository<TelegramChat, Integer> {
    TelegramChat findByChatId(long chatId);
}
