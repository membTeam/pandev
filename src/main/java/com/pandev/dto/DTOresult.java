package com.pandev.dto;

/**
 * Используется во всех методах, где требуется передача данных в виде структуры:
 * в которой содержится результат завершения, сообщение исключения и объект назначения
 * @param res результат метода
 * @param mes текстовое сообщение исключения
 * @param value передаваемый объект для вызывающего кода
 */
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
