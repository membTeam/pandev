package com.pandev.dto;

/**
 * Mapping результата запроса из репозитория
 * Используется в методе findAllGroupsToDownload репозитория
 * @param ordernum
 * @param levelnum
 * @param parenttxt
 * @param txtgroup
 */
public record DTOgroups(int ordernum, int levelnum, String parenttxt, String txtgroup) { }
