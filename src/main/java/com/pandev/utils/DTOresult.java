package com.pandev.utils;

public record DTOresult(boolean res, String mes, Object value ) {
    public static DTOresult err(String mes) {
        return new DTOresult(false, mes, null);
    }

    public static DTOresult success() {
        return new DTOresult(true, "ok", null);
    }

    public static DTOresult success(Object value) {
        return new DTOresult(true,"ok", value);
    }
}
