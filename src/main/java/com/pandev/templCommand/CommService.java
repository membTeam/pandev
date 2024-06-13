package com.pandev.templCommand;


import com.pandev.controller.ResponseHandl;
import com.pandev.repositories.GroupsRepository;
import com.pandev.utils.FileAPI;

/**
 * Функционал для классов Comd** классы алгоритма Command
 */
public interface CommService {
    GroupsRepository getGroupsRepo();

    ResponseHandl getResponseHandl();

    FileAPI getFileAPI();
}
