package com.elijahwaswa.mobilemoneyservice.service;

import com.elijahwaswa.basedomains.enums.ConditionOperator;
import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.TransactionStatus;
import com.elijahwaswa.basedomains.enums.TransactionType;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;

import java.math.BigDecimal;
import java.util.List;

//todo update by id,transactionRef,mobileMoneyRef etc

public interface TransactionService {
    String generateUniqueRefKey(TransactionEntity transactionEntity);
    TransactionDto saveTransaction(TransactionEntity transactionEntity);
    TransactionDto updateTransaction(TransactionEntity transactionEntity);
    TransactionEntity getTransaction(String transactionRef);
    TransactionEntity getTransaction(String merchantRequestId,String checkoutRequestId);
    TransactionEntity getTransactionByConversationId(String conversationId,String convoId);
    TransactionEntity getTransactionByRequestId(String requestId);
    List<TransactionDto> getTransactions(int pageNumber,int pageSize);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, BigDecimal amount);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, ConditionOperator conditionOperator,BigDecimal conditionalAmount);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, TransactionStatus transactionStatus);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, MobileMoney mobileMoney);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, TransactionType transactionType);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, String transactionRef);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, String transactionRef,MobileMoney mobileMoney);
    List<TransactionDto> getTransactions(int pageNumber, int pageSize, MobileMoney mobileMoney,String mobileMoneyRef);
}
