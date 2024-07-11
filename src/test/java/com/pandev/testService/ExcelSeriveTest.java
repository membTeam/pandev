package com.pandev.testService;

import com.pandev.dto.DTOresult;
import com.pandev.dto.RecordDTOexcel;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.service.excelService.APIGroupsNode;
import com.pandev.service.excelService.ExcelService;
import com.pandev.service.excelService.ServiceParentNode;
import com.pandev.service.excelService.ServiceSubNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ExcelSeriveTest {

    @Mock
    private GroupsRepository groupsRepo;

    @Mock
    private ServiceParentNode saveParentNode;

    @Mock
    private ServiceSubNode saveSubNode;

    @Mock
    private APIGroupsNode apiGroupsNode;

    @InjectMocks
    private ExcelService excelService;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void saveDataByExcelToDb() {

        var txtParentNode = "parentNode";
        var txtSubNode = "subNode";
        var strParams = txtParentNode + " " + txtSubNode;

        List<RecordDTOexcel> lsParams = List.of(
                RecordDTOexcel.init(strParams)
        );

        var parentNode = Groups.builder()
                .id(1)
                .rootnode(1)
                .parentnode(1)
                .txtgroup(txtParentNode)
                .levelnum(0)
                .ordernum(0)
                .build();

        var subNode = Groups.builder()
                .id(2)
                .rootnode(1)
                .parentnode(1)
                .txtgroup(txtSubNode)
                .levelnum(1)
                .ordernum(1)
                .build();

        var dtoResSubNode = DTOresult.success(subNode);
        var dtoResParentNode = DTOresult.success(parentNode);

        when(apiGroupsNode.initGroups(any(String.class), any(Groups.class))).thenReturn(subNode);
        when(saveParentNode.saveParentNode(any(String.class))).thenReturn(dtoResParentNode);
        when(saveSubNode.saveSubNode(any(Groups.class))).thenReturn(dtoResSubNode);

        var res = excelService.saveDataByExcelToDb(lsParams);

        assertTrue(res.res());
    }

    @Test
    public void readFromExcel() {
        var strFile = "temp-test.xlsx";

        var res = excelService.readFromExcel(strFile);

        assertTrue(res.size()>0);

    }

}
