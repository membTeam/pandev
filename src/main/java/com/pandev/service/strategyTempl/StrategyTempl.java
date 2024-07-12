package com.pandev.service.strategyTempl;

import com.pandev.dto.DTOresult;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface StrategyTempl {
    DTOresult applyMethod(Message message);
}
