package com.pandev.templCommand;


import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.FileAPI;
import com.pandev.controller.ResponseController;
import com.pandev.utils.excelAPI.ExcelService;

/**
 * Функционал для классов Comd** классы алгоритма Command
 */
public interface CommService {

    ExcelService getExcelService();

    GroupsRepository getGroupsRepo();

    ResponseController getResponseHandl();

    FileAPI getFileAPI();
}
