package com.pandev.serviceTest;

import com.pandev.utils.FileAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Модульный тест утилиты FileApi
 * проверка считывания файла начальной загрузки
 */
@SpringBootTest
public class FileAPITest {

    @Autowired
    private FileAPI fileAPI;

    @Test
    public void loadDataFromFile() throws IOException {
        var res = fileAPI.loadDataFromFile();

        assertTrue(res.length()>0);
    }

}
