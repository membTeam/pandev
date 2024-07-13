package com.pandev.commands;

import com.pandev.controller.MessageAPI;
import com.pandev.controller.TelegramBot;
import com.pandev.dto.DTOresult;
import com.pandev.dto.RecordDTOexcel;
import com.pandev.service.commands.Upload;
import com.pandev.service.excelService.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.pandev.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UploadTest {

    @Mock
    private ExcelService excelService;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private Message message;

    @Mock
    private Document document;

    @InjectMocks
    private MessageAPI messageAPI;

    private Upload upload;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void applyMethod() {

        Path pathTemp = Paths.get(PATH_DIR_EXTERNAL, FILE_EXCEL_TEMP);

        List<RecordDTOexcel> lsDTOexcel = List.of(
                RecordDTOexcel.init("parentNode subNode") );

        var dtoFromTelegaram = DTOresult.success(pathTemp);
        var dtoFromExcelService = new DTOresult(true, "Данные из файла загружены в БД", null);

        messageAPI.init(null, telegramBot);

        when(telegramBot.uploadDocument(any(Message.class))).thenReturn(dtoFromTelegaram);
        when(excelService.readFromExcel(any(String.class))).thenReturn(lsDTOexcel);
        when(excelService.saveDataByExcelToDb(lsDTOexcel)).thenReturn(dtoFromExcelService);

        upload = new Upload(messageAPI);
        var res = upload.applyMethod(message);

        assertTrue(res.res());

    }

}
