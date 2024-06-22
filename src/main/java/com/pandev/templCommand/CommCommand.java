package com.pandev.templCommand;

import com.pandev.controller.TelegramBot;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOparser;
import com.pandev.utils.FileAPI;
import com.pandev.utils.ParserMessage;
import com.pandev.utils.ResponseHandl;
import com.pandev.utils.excelAPI.ExcelService;
import static com.pandev.utils.Constants.*;

import org.aspectj.bridge.Constants;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.lang.reflect.Constructor;


/**
 * Класс предназначен для создания объекта из Message.getText()
 * Объект создается на основе методов рефлексии
 */
@Service
public class CommCommand implements CommService {

    private final GroupsRepository groupsRepo;
    private final FileAPI fileAPI;
    private final ExcelService excelService;
    private ResponseHandl responseHandl;

    public CommCommand(GroupsRepository groupsRepo, FileAPI fileAPI, ExcelService excelService1) {
        this.groupsRepo = groupsRepo;
        this.fileAPI = fileAPI;
        this.excelService = excelService1;
    }

    public void init(ResponseHandl responseHandl) {
        this.responseHandl = responseHandl;
    }


    private static String lowercaseFirstLetter(String word) {
        if (word.charAt(0) == '/') {
            word = word.substring(1);
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }


    public SendMessage initMessageFromStrCommand(Message message) {

        DTOparser dtoParser = ParserMessage.getParsingMessage(message);

        var strFormCommand = String.format("Comd%s",
                        lowercaseFirstLetter( dtoParser.strCommand().substring(1)));
        var pathClass = String.format("%s.%s",
                this.getClass().getPackageName(), strFormCommand);

        try {
            Class clazz = Class.forName(pathClass);
            Constructor<?> constructor = clazz.getConstructor();
            TemplCommand obj = (TemplCommand) constructor.newInstance();

            return obj.applyMethod(message, this);

        } catch (Exception e) {
            return getResponseHandl().initMessage(dtoParser.chatId(),"внутренняя ошибка.");
        }
    }

    @Override
    public ExcelService getExcelService() {
        return excelService;
    }

    @Override
    public GroupsRepository getGroupsRepo() {
        return groupsRepo;
    }

    @Override
    public ResponseHandl getResponseHandl() {
        return responseHandl;
    }

    @Override
    public FileAPI getFileAPI() {
        return fileAPI;
    }

}
