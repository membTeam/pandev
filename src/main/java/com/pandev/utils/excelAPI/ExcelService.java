package com.pandev.utils.excelAPI;

import com.pandev.controller.TelegramBot;
import com.pandev.entities.Groups;
import com.pandev.repositories.GroupsRepository;
import com.pandev.templCommand.CommCommand;
import com.pandev.utils.DTOresult;
import com.pandev.utils.InitListGroups;
import com.pandev.utils.ResponseHandl;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

                var dto = new RecordDTOexcel(cell1, cell2);
                resultData.add(dto);
            }

        } catch (Exception ex) {
            return null;
        }

        return resultData;
    }


    /**
     * Запись в поле txtgroup в строчных символах,
     * а при отображении в telegramBoт д/быть преобразование первого символа в прописной
     * @param
     * @return
     */
    @Transactional
    public DTOresult saveGroupParentFromExcel(String strRootnode) {

        /**
         * Если есть такой узел в БД -> return groupsFromRepo
         */
        var groupsFromRepo = groupsRepo.findByTxtgroup(strRootnode.trim().toLowerCase());
        if (groupsFromRepo != null) {
            return new DTOresult(true, groupsFromRepo);
        }

        Groups groups = Groups.builder()
                    .rootnode(-1)
                    .parentnode(-1)
                    .ordernum(0)
                    .levelnum(0)
                    .txtgroup(strRootnode.trim().toLowerCase())
                    .build();

        try {

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
                return new DTOresult(true, "ok");
            }

            // Позиция встраиваемого элемента в общей структуре дерева
            // относительно корневого элемента
            int subOrderNum = groupsRepo.maxOrdernum(subNode.getRootnode(), subNode.getParentnode());
            subNode.setOrdernum(subOrderNum+1);

            /**
             * записи, расположенные в структуре rootnode после вставляемой записи
             */
            var lsObjForMoved = groupsRepo.findAllGroupsByParentId(subNode.getParentnode(), subNode.getRootnode());
            if (lsObjForMoved.size() > 0) {
                List<Groups> lsGroups = InitListGroups.convListObjToListGroups(lsObjForMoved);
                lsGroups.forEach(item -> item.setOrdernum(item.getOrdernum() + 1));

                groupsRepo.saveAll(lsGroups);
            }

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
    public DTOresult saveDataByExcelToDb(List<RecordDTOexcel> lsRecordDTOExcel) {

        Map<String, Groups> mapParentNode = new HashMap<>();
        try {
            lsRecordDTOExcel.forEach(item -> {

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
            });

            return new DTOresult(true, "ok");

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }

    }

}
