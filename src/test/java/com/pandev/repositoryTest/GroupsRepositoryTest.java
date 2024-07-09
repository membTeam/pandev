package com.pandev.repositoryTest;

import com.pandev.repositories.GroupsRepository;
import com.pandev.service.excelService.ExcelService;
import com.pandev.dto.RecordDTOexcel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupsRepositoryTest {

    @Autowired
    private GroupsRepository groupsRepo;

    @Autowired
    private ExcelService excelService;


    @Test
    public void saveDataByExcelToDb_withError() {
        List<RecordDTOexcel> ls = List.of(
                RecordDTOexcel.init("js javasrc"),
                RecordDTOexcel.init("javasrc parameters"),
                RecordDTOexcel.init("javasrc func"),
                RecordDTOexcel.init("js func")
        );

        try {
            var resSave = excelService.saveDataByExcelToDb(ls);
            assertTrue(resSave.res());
        } catch (Exception ex) {
            System.out.println("typeError:" + ex.getClass().getSimpleName());
        }

    }

    @Test
    public void findAllGroups() {
        var res = groupsRepo.findAllGroupsToDownload();
        assertTrue(res.size()>0);
    }


    @Test
    public void saveDataByExcelToDb() {
        List<RecordDTOexcel> ls = List.of(
                RecordDTOexcel.init("javascript javascript"),
                RecordDTOexcel.init("javascript webdeveloper"),
                RecordDTOexcel.init("webdeveloper тестировщики"),
                RecordDTOexcel.init("webdeveloper программисты"),
                RecordDTOexcel.init("java java"),
                RecordDTOexcel.init("java джуниор"),
                RecordDTOexcel.init("java мидл"),
                RecordDTOexcel.init("java управленцы"),
                RecordDTOexcel.init("управленцы senior"),
                RecordDTOexcel.init("управленцы architect"),
                RecordDTOexcel.init("architect python"));

        var resSave = excelService.saveDataByExcelToDb(ls);

        assertTrue(resSave.res());

    }

    @Test
    public void findByTxtgroupIn() {

        List<RecordDTOexcel> ls = List.of(
                RecordDTOexcel.init("javascript javascript"),
                RecordDTOexcel.init("javascript webdeveloper"),
                RecordDTOexcel.init("webdeveloper тестировщики"),
                RecordDTOexcel.init("webdeveloper программисты"),
                RecordDTOexcel.init("java java"),
                RecordDTOexcel.init("java джуниор"),
                RecordDTOexcel.init("java мидл"),
                RecordDTOexcel.init("java управленцы"),
                RecordDTOexcel.init("управленцы senior"),
                RecordDTOexcel.init("управленцы architect"),
                RecordDTOexcel.init("architect python"));

        List<String> lsSet = ls.stream().map(item-> item.parentNode())
                .collect(Collectors.toSet()).stream().toList();
        var resRepo = groupsRepo.findByTxtgroupIn(lsSet);

        assertTrue(resRepo.size()>0);

    }

    @Test
    public void findAllGroupsByParentId() {
        var res = groupsRepo.findAllGroupsByParentId(19, 19);

        assertFalse(res.size()>0);
    }


    @Test
    public void isExistsBytxtgroupAndParentnode() {
        var res = groupsRepo.isExistsBytxtgroupAndParentnode("управленцы", 19);

        assertTrue(res);
    }

    @Test
    public void maxOrdernum() {
        var res = groupsRepo.maxOrdernum(171, 174);

        assertNotNull(res);
    }


    @Test
    public void findAllElementByRoorNode() {
        var res = groupsRepo.findAllElementByRootNode(100);
        assertTrue(res.size()>0);
    }

    @Test
    public void findByTxtgroup() {
        var firstEl = groupsRepo.firstElement();
        var res = groupsRepo.findByTxtgroup(firstEl.getTxtgroup());

        assertNotNull(res);
    }


}
