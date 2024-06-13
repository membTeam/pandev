package com.pandev.controller;

import com.pandev.repositories.GroupsRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.function.BiConsumer;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.abilitybots.api.objects.Flag;

import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class TelegramBot extends AbilityBot {

    private final String userName;

    private final ResponseHandl responseHandl;

    public TelegramBot(@Value("${BOT_TOKEN}") String token, GroupsRepository groupRepo){
        super(token, "userpandev");

        this.userName = "userpandev";
        responseHandl = new ResponseHandl(silent, db, groupRepo);
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                    (abilityBot, upd) -> responseHandl.replyToButtons(getChatId(upd), upd.getMessage());

        return Reply.of(action, Flag.TEXT,upd -> responseHandl.userIsActive(getChatId(upd)));
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(Constants.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> responseHandl.replyToStart(ctx.chatId()))
                .build();
    }

    @Override
    public long creatorId() {
        return 1L;
    }
}
