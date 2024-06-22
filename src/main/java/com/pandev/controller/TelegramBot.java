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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Service
public class TelegramBot extends AbilityBot {

    private final String externameResource;

    private final ExcelService excelService;
    private final FileAPI fileAPI;
    private final ResponseHandl responseHandl;


    public TelegramBot(@Value("${BOT_TOKEN}") String token,
                       @Value("${path-external-resource}") String eternameResource,
                       ExcelService excelService, FileAPI fileAPI, ResponseHandl responseHandl) {

        super(token, "userpandev");

        this.externameResource = eternameResource;
        this.excelService = excelService;
        this.fileAPI = fileAPI;
        this.responseHandl = responseHandl;
    }

    @PostConstruct
    private void init() {
        responseHandl.init(this.silent);
    }

    public void downloadDocument(Update update)  {

        var document = update.getMessage().getDocument();
        var chatId = update.getMessage().getChatId();
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
            excelService.saveDataByExcelToDb(lsData);

            sender.execute(
                    responseHandl.initMessage(chatId,
                            "Выполнена загрузка данных из файла"));
        } catch (Exception ex) { }
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDistributionMess(upd);

        return Reply.of(action, Flag.TEXT,upd -> responseHandl.userIsActive(getChatId(upd)));
    }

    public Reply replyToDocument() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> downloadDocument(upd);

        return Reply.of(action, Flag.DOCUMENT,upd -> replyToDocument(upd));
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


    private boolean replyToDocument(Update update) {
        return true;
    }

    @Override
    public long creatorId() {
        return 1L;
    }
}
