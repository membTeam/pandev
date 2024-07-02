package com.pandev.utils;


/**
 * Структура сообщения
 * @param chatId
 * @param strCommand
 * @param arrParams
 */
public record DTOparser(long chatId, String strCommand, String[] arrParams) { }
