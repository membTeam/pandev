package com.pandev.utils;

import com.pandev.entities.Groups;

import java.util.List;

public class InitListGroups {

    public static List<Groups> convListObjToListGroups(List<List<Object>> lsObject) {
        return lsObject.stream().map(item -> {
             return Groups.builder()
                .id((Integer) item.get(0))
                .rootnode((int) item.get(1))
                .parentnode((int) item.get(2))
                .txtgroup(item.get(3).toString())
                .ordernum((Integer) item.get(4))
                .levelnum((Integer) item.get(5))
                .build();
        }).toList();
    }
}
