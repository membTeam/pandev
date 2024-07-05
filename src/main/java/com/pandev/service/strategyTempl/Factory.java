package com.pandev.service.strategyTempl;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

@Component
public class Factory {
    private final Map<String, StrategyTempl> mapStrategyTempl;

    public Factory(Map<String, StrategyTempl> mapTemplImpl) {
        this.mapStrategyTempl = mapTemplImpl;
    }

    public StrategyTempl getBeanService(String stringType) {
        StrategyTempl strategyTempl = mapStrategyTempl.get(stringType);
        if (strategyTempl == null) {
            throw new RuntimeException("Unsupported StrategyTempl type");
        }

        return strategyTempl;
    }

    public void execute(Message message) {
        var strType = BeanType.getType(message);
        StrategyTempl strategyTempl = getBeanService(strType);
        strategyTempl.applyMethod(message);
    }

}
