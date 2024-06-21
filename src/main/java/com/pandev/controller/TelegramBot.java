package com.pandev.controller;



import com.pandev.repositories.GroupsRepository;
import com.pandev.repositories.TelegramChatRepository;
import com.pandev.templCommand.CommCommand;
import com.pandev.utils.*;
import com.pandev.utils.excelAPI.ExcelService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.Files;
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

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Service
public class TelegramBot extends AbilityBot {

    private final String userName;
    private final String externameResource;

    @Getter
    private final GroupsRepository groupsRepo;

    @Getter
    private final ExcelService excelService;

    @Getter
    private final TelegramChatRepository telegramChatRepo;

    @Getter
    private final FileAPI fileAPI;

    @Getter
    private ResponseHandl responseHandl;

    @Getter
    private CommCommand commCommand;

    public TelegramBot(@Value("${BOT_TOKEN}") String token,
                       @Value("${path-external-resource}") String eternameResource, GroupsRepository groupsRepo, ExcelService excelService,
                       TelegramChatRepository telegramChatRepo, FileAPI fileAPI,
                       GroupsRepository groupRepo
                       ){
        super(token, "userpandev");

        this.externameResource = eternameResource;
        this.groupsRepo = groupsRepo;
        this.excelService = excelService;
        this.telegramChatRepo = telegramChatRepo;
        this.fileAPI = fileAPI;

        this.userName = "userpandev";

        this.commCommand = commCommand;
        this.responseHandl = new ResponseHandl(silent, db, groupRepo);
    }

    @PostConstruct
    private void init() {
        commCommand.init(this, excelService);
        responseHandl.init(this);
    }

    public DTOresult downloadDocument(Message message) {

        var document = message.getDocument();
        var fileId = document.getFileId();

        try {
            GetFile getFile = new GetFile(fileId);
            File file = sender.execute(getFile);

            var strFile = "temp.xlsx";
            var pathExternale = Path.of(externameResource, strFile);
            Files.deleteIfExists(pathExternale);

            java.io.File tempFile = new java.io.File(pathExternale.toAbsolutePath().toString());

            downloadFile(file, tempFile);

            var lsData = excelService.readFromExcel(strFile);
            return excelService.saveDataByExcelToDb(lsData);

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDistributionMess(upd);

        return Reply.of(action, Flag.TEXT,upd -> responseHandl.userIsActive(getChatId(upd)));
    }

    public Reply replyToDocument() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDocument(upd);

        return Reply.of(action, Flag.DOCUMENT,upd -> responseHandl.userIsActive(getChatId(upd)));
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
