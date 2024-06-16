package com.pandev.utils.excelAPI;

import com.pandev.controller.TelegramBot;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.templCommand.CommCommand;
import com.pandev.utils.DTOresult;
import com.pandev.utils.ResponseHandl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XLSBUnsupportedException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    private final String PATH_DIR_EXTENAL;

    private final GroupsRepository groupsRepo;
    private ResponseHandl responseHandl;
    private CommCommand commCommand;

    public ExcelService(@Value("${path-external-resource}") String dirExtenal, TelegramBot telegramBot) {
        PATH_DIR_EXTENAL = dirExtenal;

        this.commCommand = telegramBot.getCommCommand();
        this.responseHandl = commCommand.getResponseHandl();
        this.groupsRepo = commCommand.getGroupsRepo();

    }

    public List<RecordDTOexcel> readFromExcel(String strFile) {
        var path = Paths.get(PATH_DIR_EXTENAL, strFile);
        List<RecordDTOexcel> resultData = new ArrayList<>();

        try {
            FileInputStream file = new FileInputStream(new File(path.toString()));
            Workbook workbook = new XSSFWorkbook(file);

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < 3) {
                    continue;
                }

                var cell1 = row.getCell(0).getRichStringCellValue().getString();
                var cell2 = row.getCell(1).getRichStringCellValue().getString();
                var cell3 = row.getCell(2).getRichStringCellValue().getString();

                var dto = new RecordDTOexcel(cell1, cell2, cell3);
                resultData.add(dto);
            }

        } catch (Exception ex) {
            return null;
        }

        return resultData;
    }

    public DTOresult loadExcelDataToDB(List<RecordDTOexcel> lsRecordDTOExcel) {

        List<String> lsErr = new ArrayList<>();
        try {

            lsRecordDTOExcel.stream().forEach(item -> {
                var strRoot = item.rootNode();
                var strParent = item.parentNode();
                var strGroup = item.node();

                var groupNode = groupsRepo.findByTxtgroup(strGroup);
                if (groupNode != null) {
                    throw new IllegalArgumentException("Повторный ввод:" + strGroup);
                }

                var rootNode = groupsRepo.findByTxtgroup(strRoot);
                var parenNode = groupsRepo.findByTxtgroup(strParent);

                if (rootNode == null) {
                    var resSave = commCommand.getGroupApi().saveRootNode(strRoot);
                    if (!resSave.res()) {
                        throw new IllegalArgumentException(strRoot + ": " + resSave.value().toString());
                    }

                    rootNode = (Groups) resSave.value();
                }

                if (parenNode == null) {
                    var resSave = commCommand.getGroupApi().saveSubNode(rootNode, strParent);
                    if (!resSave.res()) {
                        throw new IllegalArgumentException(strRoot + ": " + resSave.value().toString());
                    }

                    parenNode = (Groups) resSave.value();
                }

                var resSaveGroup = commCommand.getGroupApi().saveSubNode(parenNode, strGroup);
                if (!resSaveGroup.res()) {
                    throw new IllegalArgumentException(resSaveGroup.value().toString());
                }

            });
        } catch (Exception ex) {
            lsErr.add(ex.getMessage());
        }

        if (lsErr.size() > 0) {
            return new DTOresult(false, lsErr);
        }

        return new DTOresult(true, "ok");

    }

}
