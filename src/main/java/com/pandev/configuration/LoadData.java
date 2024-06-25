package com.pandev.configuration;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.excelAPI.ExcelService;
import com.pandev.utils.excelAPI.RecordDTOexcel;
import org.springframework.stereotype.Service;

/**
 * Загрузка начальных данных
 */
@Log4j
@Service
public class LoadData implements CommandLineRunner {

    private final String FILE_INITIAL_LOADING;

    private final GroupsRepository groupsRepo;
    private final ExcelService excelService;

    public LoadData(@Value("${file-initial-loading}") String initialLoading, GroupsRepository groupsRepo, ExcelService excelService) {
        FILE_INITIAL_LOADING = initialLoading;
        this.groupsRepo = groupsRepo;
        this.excelService = excelService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (groupsRepo.isExistsData()) {
            log.info("В БД имеются данные. Загрузка не требуется");
            return;
        }

        Path pathFile = Paths.get(FILE_INITIAL_LOADING);
        if (!Files.exists(pathFile)) {
            log.error("LoadData: нет файла " + FILE_INITIAL_LOADING);
            return;
        }

        List<RecordDTOexcel> lsDTOexcel = Files.readAllLines(pathFile)
                .stream()
                .map(item-> {
                    var arr = item.split("##");
                    return new RecordDTOexcel(arr[0].trim().toLowerCase(), arr[1].trim().toLowerCase());
                }).toList();

        var resSave = excelService.saveDataByExcelToDb(lsDTOexcel);
        if (!resSave.res()) {
            log.error("LoadData: " + resSave.value().toString());
        } else {
            log.info("Выполнена начальная загрузка данных в БД");
        }
    }
}
