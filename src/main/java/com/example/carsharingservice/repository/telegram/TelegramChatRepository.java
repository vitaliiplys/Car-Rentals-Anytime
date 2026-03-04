package com.example.carsharingservice.repository.telegram;

import com.example.carsharingservice.model.TelegramChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChatRepository extends JpaRepository<TelegramChat, Long> {
    boolean existsByChatId(Long chatId);
}
