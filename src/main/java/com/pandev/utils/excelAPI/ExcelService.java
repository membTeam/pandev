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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    @Transactional
    private DTOresult saveGroupParentFromExcel(List<RecordDTOexcel> lsRecordDTOExcel) {
        var setStrParentNode = lsRecordDTOExcel.stream()
                .map(item-> item.parentNode()).collect(Collectors.toSet()) ;

        List<Groups> lsGroups = new ArrayList<>();

        var valKeyId = new Object(){
            private int orderNum = -1;
            public int getRootId(){ return  --orderNum;}
            public int getParentId(){ return  orderNum;}
        };

        /**
         * Проверка наличия записей в БД
         */
        List<String> lsStrParenNode = new ArrayList<>(setStrParentNode.stream().toList());
        var lsExists = groupsRepo.findAllByTxtgroupIn(lsStrParenNode);

        // Удаление parentNode, которые есть в БД
        if (lsExists.size() > 0) {
            lsExists.forEach(item-> lsStrParenNode.remove(item.getTxtgroup()));
//            List<String> buf = lsExists.stream().map(item-> item.getTxtgroup()).toList();
            //buf.forEach(item-> lsStrParenNode.remove(item));
//            lsStrParenNode.removeAll(buf);
        }

        lsStrParenNode.forEach(item-> {
            lsGroups.add( Groups.builder()
                    .rootnode(valKeyId.getRootId())
                    .parentnode(valKeyId.getParentId())
                    .ordernum(0)
                    .levelnum(0)
                    .txtgroup(item)
                    .build() );
        });

        try {

            var resSave = groupsRepo.saveAll(lsGroups);

            resSave.forEach(item -> {
                item.setRootnode(item.getId());
                item.setParentnode(item.getId());
            });

            var afterUpdate = groupsRepo.saveAll(resSave);

            /*Map<String, Groups> mapResult = new HashMap<>();
            afterUpdate.forEach(item-> mapResult.put(item.getTxtgroup(), item));*/

            return new DTOresult(true, afterUpdate);

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }

    }

    public DTOresult saveExcelDataToDB(List<RecordDTOexcel> lsRecordDTOExcel) {

        var resSaveGroup = saveGroupParentFromExcel(lsRecordDTOExcel);
        if (!resSaveGroup.res()) {
            return new DTOresult(false, resSaveGroup.value().toString());
        }

        List<Groups> lsForOffSetOrdernum = new ArrayList<>();

//        var mapGroup = (Map<String, Groups>) resSaveGroup.value();

        var orderNumObj = new Object(){
            private int orderStart;
            private int orderNum;
            public void setStartData(int startData) {
                orderStart = startData;
                orderNum = startData;
            }
            public int getOrderNum(){ return  ++orderNum;}
            public int getDiff() {return orderNum - orderStart;}
        };

        List<Groups> lsGroups = new ArrayList<>();

        try {
            for (Groups parentNode : (List<Groups>) resSaveGroup.value()  ) {
                var lsSubNodeForParentNode = lsRecordDTOExcel.stream()
                        .filter(item -> item.parentNode().equals(parentNode.getTxtgroup())).toList();

                // Настройка счетчика orderNum
                orderNumObj.setStartData(parentNode.getOrdernum());

                var levelNum = parentNode.getLevelnum() + 1;

                lsSubNodeForParentNode.forEach(itembuffer -> {
                    lsGroups.add(Groups.builder()
                            .rootnode(parentNode.getRootnode())
                            .parentnode(parentNode.getId())
                            .ordernum(orderNumObj.getOrderNum())
                            .levelnum(levelNum)
                            .txtgroup(itembuffer.groupNode())
                            .build());
                });

                var lsForUpdateOrderNum =
                        groupsRepo.findAllGroupsBytxtGroup(parentNode.getTxtgroup(), parentNode.getRootnode());

                if (lsForUpdateOrderNum.size() > 0) {
                    List<Groups> lsGroupsOffSet = InitListGroups.convListObjToListGroups(lsForUpdateOrderNum);
                    lsGroupsOffSet.forEach(item -> item.setOrdernum(orderNumObj.getDiff()));

                    lsForOffSetOrdernum.addAll(lsGroupsOffSet);

                    groupsRepo.saveAll(lsForOffSetOrdernum);
                }
            }

            groupsRepo.saveAll(lsGroups);

            return new DTOresult(true, "ok");

        } catch (Exception ex) {
            return new DTOresult(false, ex.getMessage());
        }

    }

    public DTOresult loadExcelDataToDB(List<RecordDTOexcel> lsRecordDTOExcel) {

        List<String> lsErr = new ArrayList<>();
        try {

            /*lsRecordDTOExcel.stream().forEach(item -> {
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

            });*/
        } catch (Exception ex) {
            lsErr.add(ex.getMessage());
        }

        if (lsErr.size() > 0) {
            return new DTOresult(false, lsErr);
        }

        return new DTOresult(true, "ok");

    }

}
