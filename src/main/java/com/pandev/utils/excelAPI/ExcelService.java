package com.pandev.utils.excelAPI;


import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

import com.pandev.entities.Groups;
import com.pandev.repositories.DTOgroups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOresult;


@Service
public class ExcelService {

    private final String PATH_DIR_EXTENAL;
    private final String FILE_EXCEL_TEMPLATE;
    private final String FILE_EXCEL_DOWNLOAD;

    private final GroupsRepository groupsRepo;

    public ExcelService(@Value("${path-external-resource}") String dirExtenal,
                        @Value("${file-excel-template}") String excelTemplate,
                        @Value("${file-excel-download}") String fileexcelDownload,
                        GroupsRepository groupsRepo) {

        FILE_EXCEL_TEMPLATE = excelTemplate;
        PATH_DIR_EXTENAL = dirExtenal;
        FILE_EXCEL_DOWNLOAD = fileexcelDownload;

        this.groupsRepo = groupsRepo;
    }

    /**
     * Выгрузка данных в файл Excel
     * @return
     */
    public DTOresult downloadGroupsToExcel() {

        var objCells = new Object(){
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
        };

        Path path = Paths.get(FILE_EXCEL_TEMPLATE);
        try {
            List<DTOgroups> lsDTOgroups = groupsRepo.findAllGroupsToDownload();
            if (lsDTOgroups.size() == 0) {
                throw new IllegalArgumentException("mes:В БД нет данных для выгрузки в Excel");
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

            return new DTOresult(true, pathDownload);

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }

    }

    /**
     * Считывание данных из Excel file
     * Сканирование строк завершается, если значение cell is null
     * @param strFile
     * @return
     */
    public List<RecordDTOexcel> readFromExcel(String strFile) {
        var path = Paths.get(PATH_DIR_EXTENAL, strFile);
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

                var cell1 = row.getCell(0).getRichStringCellValue().getString();
                var cell2 = row.getCell(1).getRichStringCellValue().getString();

                var dto = new RecordDTOexcel(cell1, cell2);
                resultData.add(dto);
            }

            return resultData;

        } catch (Exception ex) {
            resultData.clear();
            return resultData;
        }
    }


    /**
     * Запись в поле txtgroup в строчных символах,
     * а при отображении в telegramBoт д/быть преобразование первого символа в прописной
     * @param
     * @return
     */
    @Transactional
    public DTOresult saveGroupParentFromExcel(String strRootnode) {

        strRootnode = strRootnode.trim().toLowerCase();

        try {

            /**
             * Если есть такой узел в БД -> return groupsFromRepo
             */
            var groupsFromRepo = groupsRepo.findByTxtgroup(strRootnode);
            if (groupsFromRepo != null) {
                return new DTOresult(true, groupsFromRepo);
            }

            Groups groups = Groups.builder()
                    .rootnode(-1)
                    .parentnode(-1)
                    .ordernum(0)
                    .levelnum(0)
                    .txtgroup(strRootnode)
                    .build();


            var resSave = groupsRepo.save(groups);

            resSave.setParentnode(resSave.getId());
            resSave.setRootnode(resSave.getId());

            var afterSave = groupsRepo.save(resSave);

            return new DTOresult(true, afterSave);

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }

    }


    /**
     * Сохранение дочернего узла и изменение поля ordernum (индекс очередности в контексте rootnode)
     * у всех последующих записей после вставляемой
     * @param subNode
     * @return
     */
    @Transactional
    private DTOresult saveSubNodeFromExcel(Groups subNode) {

        try {
            // Исключается дублирование txtgroup and parentNode
            if (groupsRepo.isExistsBytxtgroupAndParentnode(subNode.getTxtgroup(), subNode.getParentnode())) {
                return new DTOresult(true, "Повторный ввод субЭлемента");
            }

            // Позиция встраиваемого элемента в общей структуре дерева
            // относительно корневого элемента
            int subMaxOrderNum = groupsRepo.maxOrdernum(subNode.getRootnode(), subNode.getParentnode());
            subNode.setOrdernum(subMaxOrderNum+1);

            groupsRepo.save(subNode);

            return new DTOresult(true, subNode);

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }

    }

    /**
     * Обработка делается в последовательности:
     * если нет родительского элемента -> создается в saveGroupParentFromExcel.
     * По каждому объекту из параметра lsRecordDTOExcel делается запись в БД.
     * Т.образом сколько записей в excel столько раз будет делаться запись в БД.
     * Для другого подхода необходимо согласование по ТЗ.
     * @param lsRecordDTOExcel создается из readFromExcel
     * @return
     */
    @Transactional
    public DTOresult saveDataByExcelToDb(List<RecordDTOexcel> lsRecordDTOExcel) {

         Map<String, Groups> mapParentNode = new HashMap<>();
        try {
            for(var item: lsRecordDTOExcel) {

                Groups parentNode = mapParentNode.get(item.parentNode().trim().toLowerCase());
                if (parentNode == null) {
                    var resGroup = saveGroupParentFromExcel(item.parentNode());
                    if (!resGroup.res()) {
                        throw new IllegalArgumentException(resGroup.value().toString());
                    }

                    parentNode = (Groups) resGroup.value();
                    mapParentNode.put(parentNode.getTxtgroup(), parentNode );
                }

                Groups subGroups = Groups.builder()
                        .rootnode(parentNode.getRootnode())
                        .parentnode(parentNode.getId())
                        .levelnum(parentNode.getLevelnum()+1)
                        .ordernum(0)    // назначается в saveSubNodeFromExcel
                        .txtgroup(item.groupNode().trim().toLowerCase())
                        .build();

                var resSaveSubNode = saveSubNodeFromExcel(subGroups);

                if (!resSaveSubNode.res()) {
                    throw new IllegalArgumentException(resSaveSubNode.value().toString());
                }
            }

            return new DTOresult(true, "Данные из файла загружены в БД");

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }

    }

}
