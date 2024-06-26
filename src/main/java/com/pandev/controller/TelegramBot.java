package com.pandev.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

import jakarta.annotation.PostConstruct;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import com.pandev.utils.Constants;
import com.pandev.utils.FileAPI;
import com.pandev.utils.excelAPI.ExcelService;

@Service
public class TelegramBot extends AbilityBot {

    private final String externameResource;

    private final ExcelService excelService;
    private final FileAPI fileAPI;
    private final ResponseHandler responseHandl;
    private final MessageAPI messageAPI;


    public TelegramBot(@Value("${BOT_TOKEN}") String token,
                       @Value("${path-external-resource}") String eternameResource,
                       ExcelService excelService, FileAPI fileAPI,
                       ResponseHandler responseHandl, MessageAPI messageAPI) {

        super(token, "userpandev");

        this.externameResource = eternameResource;
        this.excelService = excelService;
        this.fileAPI = fileAPI;
        this.responseHandl = responseHandl;
        this.messageAPI = messageAPI;
    }

    @PostConstruct
    private void init() {
        responseHandl.init(this);
        messageAPI.init(this);
    }

    private void uploadDocument(Update update)  {

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
                    MessageAPI.initMessage(chatId,
                            "Выполнена загрузка данных из файла"));
        } catch (Exception ex) { }
    }

    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDistributionMess(upd);

        return Reply.of(action, Flag.TEXT,upd -> true);
    }

    public Reply replyToDocument() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> uploadDocument(upd);

        return Reply.of(action, Flag.DOCUMENT,upd -> true);
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(Constants.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> responseHandl.replyToDistributionMess(ctx.update()))
                .build();
    }

    @Override
    public long creatorId() {
        return 1L;
    }
}
