package com.pandev.commands;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import static com.pandev.utils.Constants.PATH_DIR_EXTERNAL;

import com.pandev.dto.DTOresult;
import com.pandev.service.commands.Download;
import com.pandev.service.excelService.ExcelService;

public class DownloadTest {

    @Mock
    private Message message;

    @Mock
    private ExcelService excelService;

    @InjectMocks
    private Download download;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void applyMethod_NotDataInDB() {

        var dto = DTOresult.err("mes:В БД нет данных для выгрузки в Excel");

        when(message.getChatId()).thenReturn(1L);
        when(excelService.downloadGroupsToExcel()).thenReturn(dto);

        var res = download.applyMethod(message);

        assertFalse(res.res());
    }


    @Test
    public void applyMethod() {

        Path pathDTOResult = Paths.get(PATH_DIR_EXTERNAL, "download-test.xlsx");

        var dto = DTOresult.success(pathDTOResult);

        when(message.getChatId()).thenReturn(1L);
        when(excelService.downloadGroupsToExcel()).thenReturn(dto);

        var res = download.applyMethod(message);
        assertTrue(res.res());

        var document = ((SendDocument)res.value()).getDocument();
        assertEquals("attach://download-test.xlsx", document.getAttachName());

    }

}
