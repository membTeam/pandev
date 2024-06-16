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
    public void readFromExcel() {
        var res = excelService.readFromExcel("test-excel.xlsx");

        var resSave = excelService.saveExcelDataToDB(res);

        excelService.loadExcelDataToDB(res);

        assertNotNull(res);
    }

}
