package com.pandev.utilExcelTest;


import com.pandev.utils.excelAPI.ExcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hibernate.validator.internal.util.Contracts.*;

@SpringBootTest
public class ExcelServiceTest {

    @Autowired
    private ExcelService excelService;


    @Test
    public void readFromExcel_readData() {
        var res = excelService.readFromExcel("temp.xlsx");

        assertNotNull(res);
    }

    @Test
    public void readFromExcel() {
        var res = excelService.readFromExcel("temp.xlsx");

        var resSave = excelService.saveDataByExcelToDb(res);

        assertNotNull(resSave.res());
    }

}
