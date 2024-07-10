package com.pandev;

import com.pandev.dto.DTOresult;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.excelService.ExcelService;
import com.pandev.service.excelService.ServiceParentNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ExcelServiceTest {

    @Autowired
    private ExcelService excelService;

    @MockBean
    private GroupsRepository groupsRepo;

    @MockBean
    private ServiceParentNode serviceParentNode;


    @Test
    public void readFromExcel() {
       /* var res = excelService.readFromExcel("temp-test.xlsx");

        assertTrue(res.size()>0);*/
    }

/*
    @Test
    public void saveDataByExcelToDb() {
        var lsRes = excelService.readFromExcel("temp-test.xlsx");
        var parentGroups = Groups.builder()
                .id(10)
                .rootnode(10)
                .parentnode(10)
                .txtgroup(lsRes.get(0).parentNode())
                .build();

        var subGroups = Groups.builder()
                .id(11)
                .rootnode(10)
                .parentnode(10)
                .txtgroup(lsRes.get(0).groupNode())
                .build();

        var dtoParent = DTOresult.success(parentGroups);

        when(serviceParentNode.saveParentNode(any(String.class))).thenReturn(dtoParent);
        when(groupsRepo.save(any(Groups.class))).thenReturn(subGroups);

        var resSave = excelService.saveDataByExcelToDb(lsRes);
        assertTrue(resSave.res());

    }*/
}
