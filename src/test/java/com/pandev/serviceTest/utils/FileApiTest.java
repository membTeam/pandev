package com.pandev.serviceTest.utils;


import com.pandev.utils.FileAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FileApiTest {

    @Autowired
    private FileAPI fileAPI;

    @Test
    public void loadDataFromFile() throws IOException {
        var res = fileAPI.loadDataFromFile("comd_help.txt");

        assertTrue(res.length()>0);
    }

}
