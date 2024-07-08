package com.pandev.dto;

/**
 * Используется для отображения запроса getTreeData в строковый формат TreeView
 * @param levelnum
 * @param txtgroup
 */
public record GroupsDetails(int levelnum, String txtgroup) {
}
