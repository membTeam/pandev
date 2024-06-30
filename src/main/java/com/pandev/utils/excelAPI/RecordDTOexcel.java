package com.pandev.utils.excelAPI;

public record RecordDTOexcel(String parentNode, String groupNode) {

    public static RecordDTOexcel init(String str) {
        var index = str.indexOf(" ");
        return new RecordDTOexcel(str.substring(0, index), str.substring(index).trim() );
    }

}
