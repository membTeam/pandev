package com.pandev.utils;

import com.pandev.repositories.GroupsRepository;

public class InitListViewWithFormated {

    private static String convChar(String str) {
        var result = str.trim().toUpperCase();
        result = result.substring(0,1) + result.substring(1).toLowerCase();

        return result;
    }

    public static String initViewFormated(GroupsRepository repo) {

        var lsGroups = repo.getTreeData().stream()
                .map(item-> "*".repeat(item.levelnum()) + convChar(item.txtgroup()))
                .toArray();

        var sb = new StringBuffer();
        for (var item : lsGroups) {
            sb.append(item + "\n");
        }

        return sb.toString();
    }
}
