package com.example.carsharingservice.service.notification.impl;

import com.example.carsharingservice.service.notification.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateScheduler {
    private static final String GET_UPDATES_URL =
            "https://api.telegram.org/bot%s/getUpdates?offset=%d";

    private final RestTemplate restTemplate;
    private final NotificationService notificationService;

    @Value("${telegram.bot.token}")
    private String botToken;

    private long offset = 0;

    @Scheduled(fixedDelay = 5000)
    public void pollUpdates() {
        try {
            String url = String.format(GET_UPDATES_URL, botToken, offset);
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.path("ok").asBoolean()) {
                return;
            }

            for (JsonNode update : response.path("result")) {
                long updateId = update.path("update_id").asLong();
                JsonNode message = update.path("message");

                if (!message.isMissingNode()) {
                    long chatId = message.path("chat").path("id").asLong();
                    notificationService.saveChatId(chatId);
                }

                offset = updateId + 1;
            }
        } catch (Exception e) {
            log.error("Failed to poll Telegram updates: {}", e.getMessage());
        }
    }
}
