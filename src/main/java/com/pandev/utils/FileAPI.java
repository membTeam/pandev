package com.pandev.utils;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.pandev.utils.Constants.PATH_TXT_MESSAGE;

/**
 * Используется для загрузки данных из файла.
 * Директория прописана в application.porperties
 */
@Service
@NoArgsConstructor
public class FileAPI {

    /**
     * Загрузка текстового файла - описание команд telegramBot
     * @return
     * @throws IOException
     */
    public String loadTxtDataFromFile(String fileName) throws IOException {

        Path path = Paths.get(PATH_TXT_MESSAGE, fileName);

        byte[] byteFromFile = Files.readAllBytes(path);

        return new String(byteFromFile);
    }

}
