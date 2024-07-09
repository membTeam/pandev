package com.pandev.dto;


/**
 * Для парсинга структуры класса Message, на составляющие
 * Создается в классе ParserMessage.getParsingMessage
 * Используется в RemoveElement, AddElement, ResponseHandler
 * @param chatId Id chat
 * @param strCommand используется в ResponseHandler.replyToDistributionMess
 * @param arrParams arrParams[0] command from telegramGot, arrParams[1] параметр команды telegramBot
 */
public record DTOparser(long chatId, String strCommand, String[] arrParams) { }
