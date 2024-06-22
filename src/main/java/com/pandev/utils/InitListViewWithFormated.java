package com.pandev.utils;

import com.pandev.repositories.GroupsRepository;

/**
 * Форматированная строка дерева групп
 */
public class InitListViewWithFormated {

    /**
     * Преобразование первого символа в строчный
     * @param str
     * @return
     */
    private static String convChar(String str) {
        var result = str.trim().toUpperCase();
        result = result.substring(0,1) + result.substring(1).toLowerCase();

        return result;
    }

    /**
     * Преобразование данных таблицы groups into formated string
     * @param repo
     * @return
     */
    public static String initViewFormated(GroupsRepository repo) {
        var sb = new StringBuffer();

        repo.getTreeData().stream()
                .map(item-> "*".repeat(item.levelnum()) + convChar(item.txtgroup()))
                .toList().forEach(item-> sb.append(item + "\n"));

        return sb.toString();
    }
}
