package com.pandev.utils.excelAPI;


import com.pandev.entities.Groups;
import com.pandev.repositories.DTOgroups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.DTOresult;
import lombok.extern.log4j.Log4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j
@Service
public class ExcelService {

    private final String PATH_DIR_EXTENAL;
    private final String FILE_EXCEL_TEMPLATE;
    private final String FILE_EXCEL_DOWNLOAD;

    private final GroupsRepository groupsRepo;

    private final SaveGroupParentNode saveParentNode;
    private final SaveGroupsSubNode saveSubNode;
    private final GetGroupsNode getGroupsNode;

    public ExcelService(@Value("${path-external-resource}") String dirExtenal,
                        @Value("${file-excel-template}") String excelTemplate,
                        @Value("${file-excel-download}") String fileexcelDownload,
                        GroupsRepository groupsRepo, SaveGroupParentNode saveGroupParentFromExcel, SaveGroupsSubNode saveGroupsSubNode, GetGroupsNode getGroupsNode) {

        FILE_EXCEL_TEMPLATE = excelTemplate;
        PATH_DIR_EXTENAL = dirExtenal;
        FILE_EXCEL_DOWNLOAD = fileexcelDownload;

        this.groupsRepo = groupsRepo;
        this.saveParentNode = saveGroupParentFromExcel;
        this.saveSubNode = saveGroupsSubNode;
        this.getGroupsNode = getGroupsNode;
    }

    /**
     * Выгрузка данных в файл Excel
     * @return
     */
    @Transactional(readOnly = true)
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
                throw new RuntimeException("mes:В БД нет данных для выгрузки в Excel");
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
    @Transactional(propagation = Propagation.NESTED)
    public DTOresult saveGroupParentFromExcel(String strRootnode) {

        strRootnode = strRootnode.trim().toLowerCase();

        try {

            /**
             * Если есть такой узел в БД -> return groupsFromRepo
             */
            var groupsFromRepo = groupsRepo.findByTxtgroup(strRootnode);
            if (groupsFromRepo != null) {
                throw new RuntimeException("Повторный ввод rootNode:" + strRootnode);
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

            return DTOresult.success(afterSave);

        } catch (Exception ex) {
            log.error("Повторный ввод rootNode: " + strRootnode);
            throw ex;
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
                return new DTOresult(true, "exists", null);
            }

            /**
             * Позиция встраиваемого элемента в общей структуре дерева
             * относительно корневого элемента
             */
            int subMaxOrderNum = groupsRepo.maxOrdernum(subNode.getRootnode(), subNode.getParentnode());
            subNode.setOrdernum(++subMaxOrderNum);

            groupsRepo.save(subNode);

            return DTOresult.success(subNode);

        } catch (Exception ex) {
            return DTOresult.err(ex.getMessage());
        }

    }

    /**
     * Предварительная загрузка из БД
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

    private Groups initGroups(String txtSubNode, Groups groups) {

        Groups subGroups = Groups.builder()
                .rootnode(groups.getRootnode())
                .parentnode(groups.getId())
                .levelnum(groups.getLevelnum() + 1)
                .ordernum(0)    // назначается в saveSubNodeFromExcel
                .txtgroup(txtSubNode.trim().toLowerCase())
                .build();

        return  subGroups;
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
    @Transactional(noRollbackFor = RuntimeException.class)
    public DTOresult saveDataByExcelToDb(List<RecordDTOexcel> lsRecordDTOExcel) {

        DTOresult dtoResult = null;
        int index = 0;

        try {

            for (var item : lsRecordDTOExcel) {
                var txtParentNode = item.parentNode();
                var txtSubNode = item.groupNode().trim().toLowerCase();

                if (getGroupsNode.isExistsGroupNode(txtSubNode)) {
                    continue;
                }

                var parentNode = (Groups) saveParentNode.saveGroupParentFromExcel(txtParentNode).value();

                if (parentNode.getTxtgroup().trim().toLowerCase().equals(txtSubNode)) {
                    continue;
                }

                Groups subGroups = initGroups(txtSubNode, parentNode);

                saveSubNode.saveGroupsSubNode(subGroups);

                if (++index > 1) {
                    throw new RuntimeException();
                }
            }

            dtoResult = new DTOresult(true, "Данные из файла загружены в БД", null);

        } catch (Exception ex) {
            log.error("saveDataByExcelToDb: " + ex.getMessage());
            dtoResult = DTOresult.err("Не известная ошибка записи в БД из Excel");
        }

        return dtoResult;
    }

}
