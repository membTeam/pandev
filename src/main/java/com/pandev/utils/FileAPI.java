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

    public FileAPI(@Value("${file.load-data}") String file,
                   @Value("${directory.load-data}") String dir
                   ) {
        pathLoadData = Paths.get(dir + '/' + file);
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

}
