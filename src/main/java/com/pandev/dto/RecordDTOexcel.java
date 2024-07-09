package com.pandev.dto;


/**
 * Класс типа DTO при обработке Excel файла
 * @param parentNode
 * @param groupNode
 */
public record RecordDTOexcel(String parentNode, String groupNode) {

    public static RecordDTOexcel init(String str) {
        var index = str.indexOf(" ");
        return new RecordDTOexcel(str.substring(0, index), str.substring(index).trim() );
    }

}
