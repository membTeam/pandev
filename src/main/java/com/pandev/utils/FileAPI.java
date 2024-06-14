package com.pandev.utils;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Используется для загрузки данных из файла.
 * Используемая директория прописана в application.porperties
 */
@Service
public class FileAPI {

    private final Path pathLoadData;
    private final Path pathDir;
    private final Path pathDirMessage;

    public FileAPI(@Value("${file.load-data}") String file,
                   @Value("${directory.load-data}") String dir,
                   @Value("${directory-txt-message}") String dirMessage
                   ) {
        pathLoadData = Paths.get(dir + '/' + file);
        pathDir = Paths.get(dir);
        pathDirMessage = Paths.get(dirMessage);
    }

    /**
     * Загрузка файла начальных данных.
     * @return
     * @throws IOException
     */
    public String loadDataFromFile() throws IOException {

        byte[] byteFromFile = Files.readAllBytes(pathLoadData);

        return new String(byteFromFile);

    }

    /**
     * Загрузка текстового файла - описание команд telegramBot
     * @return
     * @throws IOException
     */
    public String loadDataFromFile(String fileName) throws IOException {

        Path path = pathDirMessage.resolve(fileName);
        byte[] byteFromFile = Files.readAllBytes(path);

        return new String(byteFromFile);

    }

}
