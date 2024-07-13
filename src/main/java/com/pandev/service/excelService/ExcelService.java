package com.pandev.service.excelService;


import com.pandev.dto.DTOgroups;
import com.pandev.dto.DTOresult;
import com.pandev.dto.RecordDTOexcel;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pandev.utils.Constants.*;

/**
 * Основной сервис загрузки/выгрузки данных в Excel
 */
@Log4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    private final GroupsRepository groupsRepo;
    private final ServiceParentNode saveParentNode;
    private final ServiceSubNode saveSubNode;
    private final APIGroupsNode apiGroupsNode;


    /**
     * Вспомогательный локальный класс
     * Инициализация ячеек Excel
     * Используется в методе downloadGroupsToExcel()
     */
    private class ObjCells {
        private int rowNum = 1;
        private int indexNum = 1;

        public Cell createCell(Workbook wb, Row row, int numColumn, HorizontalAlignment align) {
            var cell = row.createCell(numColumn);
            var cellStyle = wb.createCellStyle();

            cellStyle.setAlignment(align);
            cell.setCellStyle(cellStyle);

            return cell;
        }

        public int getRowNum() {
            return rowNum++;
        }
        public int getIndexNum() {
            return indexNum++;
        }
    }

    /**
     * Выгрузка данных в файл Excel
     * @return
     */
    public DTOresult downloadGroupsToExcel() {

        var objCells = new ObjCells();

        try {
            Path path = Paths.get(FILE_EXCEL_TEMPLATE);

            List<DTOgroups> lsDTOgroups = groupsRepo.findAllGroupsToDownload();
            if (lsDTOgroups.size() == 0) {
                return DTOresult.err("mes:В БД нет данных для выгрузки в Excel");
            }

            FileInputStream file = new FileInputStream(new File(path.toString()));

            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            lsDTOgroups.forEach(dtoExcel-> {
                Row row = sheet.createRow(objCells.getRowNum());
                int numCell = -1;

                while (++numCell < 5) {
                    var hAlignment = switch (numCell) {
                        case 0,1,2 -> HorizontalAlignment.CENTER;
                        default -> HorizontalAlignment.LEFT;
                    };

                    var cell = objCells.createCell(workbook, row, numCell, hAlignment);
                    switch (numCell) {
                        case 0 -> cell.setCellValue(objCells.getIndexNum());
                        case 1 -> cell.setCellValue(dtoExcel.ordernum());
                        case 2 -> cell.setCellValue(dtoExcel.levelnum());
                        case 3 -> cell.setCellValue(dtoExcel.parenttxt());
                        default -> cell.setCellValue(dtoExcel.txtgroup());
                    }
                }
            });

            Path pathDownload = Paths.get(FILE_EXCEL_DOWNLOAD);

            Files.deleteIfExists(pathDownload);

            FileOutputStream outputStream = new FileOutputStream(pathDownload.toAbsolutePath().toString());
            workbook.write(outputStream);
            workbook.close();

            return DTOresult.success(pathDownload);

        } catch (Exception ex) {
            log.error("downloadGroupsToExcel: " + ex.getMessage());
            return DTOresult.err(ex.getMessage());
        }

    }

    /**
     * Считывание данных из Excel file.
     * Сканирование строк завершается, если значение cell is null
     * @param strFile
     * @return
     */
    public List<RecordDTOexcel> readFromExcel(String strFile) {
        var path = Paths.get(PATH_DIR_EXTERNAL, strFile);
        List<RecordDTOexcel> resultData = new ArrayList<>();

        try {
            FileInputStream file = new FileInputStream(new File(path.toString()));
            Workbook workbook = new XSSFWorkbook(file);

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < 1) {
                    continue;
                }

                // Завершение сканирования строк (Row)
                if (row.getCell(0) == null) {
                    break;
                }

                var parentCell = row.getCell(0).getRichStringCellValue().getString();
                var subCell = row.getCell(1).getRichStringCellValue().getString();

                var dto = new RecordDTOexcel(parentCell, subCell);
                resultData.add(dto);
            }

            return resultData;

        } catch (Exception ex) {
            resultData.clear();
            return resultData;
        }
    }

    /**
     * Предварительная загрузка объектов, которые есть в БД
     * @param map
     * @param lsRecordDTOExcel
     * @param isParentNode
     */
    @Transactional(readOnly = true)
    public void initMapParentNode(Map<String, Groups> map, List<RecordDTOexcel> lsRecordDTOExcel, boolean isParentNode) {
        List<String> lsSet;
        if (isParentNode) {
            lsSet = lsRecordDTOExcel.stream().map(item -> item.parentNode().trim().toLowerCase() )
                    .collect(Collectors.toSet()).stream().toList();
        } else {
            lsSet = lsRecordDTOExcel.stream().map(item -> item.groupNode().trim().toLowerCase())
                    .collect(Collectors.toSet()).stream().toList();
        }

        var resRepo = groupsRepo.findByTxtgroupIn(lsSet);
        if (resRepo.size() > 0) {
            resRepo.forEach(item-> map.put(item.getTxtgroup().trim().toLowerCase(), item) );
        }
    }

    /**
     * Обработка делается в последовательности, если нет parentNode -> создается parentNode,
     * а затем делается запись дочернего узла, если его нет в БД.
     * @param lsRecordDTOExcel создается из readFromExcel
     * @return
     */
    @Transactional
    public DTOresult saveDataByExcelToDb(List<RecordDTOexcel> lsRecordDTOExcel) {

        Map<String, Groups> mapSubGroups = new HashMap<>();

        initMapParentNode(mapSubGroups, lsRecordDTOExcel, false);

        for (var item : lsRecordDTOExcel) {
            var txtParentNode = item.parentNode().trim().toLowerCase();
            var txtSubNode = item.groupNode().trim().toLowerCase();

            if (txtParentNode.equals(txtSubNode) || mapSubGroups.containsKey(txtSubNode)) {
                continue;
            }

            var parentNode = (Groups) saveParentNode.saveParentNode(txtParentNode).value();
            mapSubGroups.put(parentNode.getTxtgroup().trim().toLowerCase(), parentNode);

            Groups subGroups = apiGroupsNode.initGroups(txtSubNode, parentNode);

            var groupsSaved = (Groups) saveSubNode.saveSubNode(subGroups).value();

            mapSubGroups.put(groupsSaved.getTxtgroup().trim().toLowerCase(), groupsSaved);
        }

        return new DTOresult(true, "Данные из файла загружены в БД", null);

    }

}
