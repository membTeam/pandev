package com.pandev.commands;

import com.pandev.controller.MessageAPI;
import com.pandev.dto.DTOresult;
import com.pandev.service.commands.Upload;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.pandev.utils.Constants.FILE_EXCEL_TEMPLATE;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UploadTest {

    @Autowired
    private MessageAPI messageAPI;

    @Autowired
    private Upload upload;

    @MockBean
    private Message message;

    @MockBean
    private Document document;

    @Test
    public void applyMethod() {
        when(message.getChatId()).thenReturn(1L);

        InputFile document = new InputFile(new java.io.File(FILE_EXCEL_TEMPLATE));

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(1L);
        sendDocument.setCaption("Дерево групп в формате Excel");
        sendDocument.setDocument(document);

        var res = upload.applyMethod(message);


    }

}
