package com.pandev.controller;


import com.pandev.repositories.TelegramChatRepository;
import com.pandev.templCommand.CommCommand;
import com.pandev.utils.Constants;
import com.pandev.utils.DTOresult;
import com.pandev.utils.FileAPI;
import com.pandev.utils.ResponseHandl;
import com.pandev.utils.excelAPI.ExcelService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Service
public class TelegramBot extends AbilityBot {

    private final String userName;
    private final String externameResource;

    private final ExcelService excelService;
    private final FileAPI fileAPI;
    private final ResponseHandl responseHandl;


    public TelegramBot(@Value("${BOT_TOKEN}") String token,
                       @Value("${path-external-resource}") String eternameResource,
                       ExcelService excelService, FileAPI fileAPI, ResponseHandl responseHandl) {

        super(token, "userpandev");
        this.userName = "userpandev";

        this.externameResource = eternameResource;
        this.excelService = excelService;
        this.fileAPI = fileAPI;
        this.responseHandl = responseHandl;
    }

    @PostConstruct
    private void init() {
        responseHandl.init(this.silent);
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
