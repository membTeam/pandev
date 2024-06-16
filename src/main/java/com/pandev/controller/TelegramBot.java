package com.pandev.controller;



import com.pandev.repositories.GroupsRepository;
import com.pandev.repositories.TelegramChatRepository;
import com.pandev.templCommand.CommCommand;
import com.pandev.utils.Constants;
import com.pandev.utils.FileAPI;
import com.pandev.utils.GroupsApi;
import com.pandev.utils.ResponseHandl;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Service
public class TelegramBot extends AbilityBot {

    private final String userName;
    private final String externameResource;

    @Getter
    private final GroupsApi groupsApi;

    @Getter
    private final TelegramChatRepository telegramChatRepo;

    @Getter
    private final FileAPI fileAPI;

    @Getter
    private ResponseHandl responseHandl;

    @Getter
    private CommCommand commCommand;

    public TelegramBot(@Value("${BOT_TOKEN}") String token,
                       @Value("${path-external-resource}") String eternameResource, GroupsApi groupsApi,
                       TelegramChatRepository telegramChatRepo, FileAPI fileAPI,
                       GroupsRepository groupRepo,
                       CommCommand commCommand ){
        super(token, "userpandev");

        this.externameResource = eternameResource;
        this.groupsApi = groupsApi;
        this.telegramChatRepo = telegramChatRepo;
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

    public File downloadDocument(Message message) throws TelegramApiException {

        var document = message.getDocument();
        var fileId = document.getFileId();

        GetFile getFile = new GetFile(fileId);

        File file = sender.execute(getFile);

        var strPathExternale = Path.of(externameResource, "temp.excel").toString();
        java.io.File tempFile = new java.io.File(strPathExternale);

        downloadFile(file, tempFile);

        return file;
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDistributionMess(upd);

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
