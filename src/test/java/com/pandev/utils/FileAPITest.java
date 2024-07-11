package com.pandev.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileAPITest {

    @Test
    public void loadTxtDataFromFile() throws IOException {
        var file = "comd_help.txt";
        var fileAPI = new FileAPI();

        var resRead = fileAPI.loadTxtDataFromFile(file);

        assertTrue(resRead.length()>0);
    }

}
