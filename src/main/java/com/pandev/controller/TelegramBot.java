package com.pandev.controller;

import com.pandev.dto.DTOresult;
import jakarta.annotation.PostConstruct;
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
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static com.pandev.utils.Constants.*;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

import com.pandev.utils.Constants;


@Service
public class TelegramBot extends AbilityBot {

    private final ResponseHandler responseHandl;
    private final MessageAPI messageAPI;

    public TelegramBot(
            @Value("${BOT_TOKEN}") String token,
                       MessageAPI messageAPI,
                       ResponseHandler responseHandl) {
        super(token, "corseProj5bot");

        this.responseHandl = responseHandl;
        this.messageAPI = messageAPI;
    }

    @PostConstruct
    private void init() {
        messageAPI.init(silent, this);
    }


    public DTOresult uploadDocument(Message message) {

        try {
            var document = message.getDocument();
            var fileId = document.getFileId();

            GetFile getFile = new GetFile(fileId);

            File file = sender().execute(getFile);

            var pathExternale = Path.of(PATH_DIR_EXTERNAL, FILE_EXCEL_TEMP);
            Files.deleteIfExists(pathExternale);

            java.io.File tempFile = new java.io.File(pathExternale.toAbsolutePath().toString());

            downloadFile(file, tempFile);

            return DTOresult.success(Paths.get(FILE_EXCEL_TEMP));

        } catch (Exception ex) {
            return DTOresult.err("Не известная ошибка загрузки данных из Excel");
        }
    }


    public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDistributionMess(upd);

        return Reply.of(action, Flag.TEXT,upd -> true);
    }

    public Reply replyToDocument() {
        BiConsumer<BaseAbilityBot, Update> action =
                (abilityBot, upd) -> responseHandl.replyToDistributionMess(upd);

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
