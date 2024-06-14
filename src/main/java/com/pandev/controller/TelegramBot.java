package com.pandev.controller;

import com.pandev.repositories.GroupsRepository;
import com.pandev.templCommand.CommCommand;
import com.pandev.utils.Constants;
import com.pandev.utils.FileAPI;
import com.pandev.utils.ResponseHandl;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.abilitybots.api.objects.Flag;

import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Service
public class TelegramBot extends AbilityBot {

    private final String userName;

    @Getter
    private final FileAPI fileAPI;

    @Getter
    private ResponseHandl responseHandl;

    @Getter
    private CommCommand commCommand;

    public TelegramBot(@Value("${BOT_TOKEN}") String token, FileAPI fileAPI,
                       GroupsRepository groupRepo,
                       CommCommand commCommand ){
        super(token, "userpandev");
        this.fileAPI = fileAPI;

        this.userName = "userpandev";

        this.commCommand = commCommand;
        this.responseHandl = new ResponseHandl(silent, db, groupRepo);
    }

    @PostConstruct
    private void init() {
        commCommand.init(this);
        responseHandl.init(this);
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDistributionMess(upd);

        return Reply.of(action, Flag.TEXT,upd -> responseHandl.userIsActive(getChatId(upd)));
    }


    /*public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                    (abilityBot, upd) -> responseHandl.replyToButtons(getChatId(upd), upd.getMessage());

        return Reply.of(action, Flag.TEXT,upd -> responseHandl.userIsActive(getChatId(upd)));
    }*/

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
