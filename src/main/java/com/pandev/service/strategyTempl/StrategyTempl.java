package com.pandev.service.strategyTempl;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface StrategyTempl {
    void applyMethod(Message message);
}
