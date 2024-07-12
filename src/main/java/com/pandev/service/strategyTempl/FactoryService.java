package com.pandev.service.strategyTempl;

import com.pandev.dto.DTOresult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class FactoryService {
    private final Factory beanFactory;

    public DTOresult responseToMessage(Message message) {
        return beanFactory.execute(message);
    }

}
