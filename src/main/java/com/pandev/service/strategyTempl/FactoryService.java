package com.pandev.service.strategyTempl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class FactoryService {
    private final Factory beanFactory;

    public void responseToMessage(Message message) {
        beanFactory.execute(message);
    }

}
