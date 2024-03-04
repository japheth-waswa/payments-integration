package com.elijahwaswa.mobilemoneyservice.service.impl;

import com.elijahwaswa.basedomains.enums.ConditionOperator;
import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.TransactionStatus;
import com.elijahwaswa.basedomains.enums.TransactionType;
import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.basedomains.utils.Helpers;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;
import com.elijahwaswa.mobilemoneyservice.exception.ResourceNotFoundException;
import com.elijahwaswa.mobilemoneyservice.repository.TransactionRepository;
import com.elijahwaswa.mobilemoneyservice.service.TransactionService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private TransactionRepository transactionRepository;
    private ModelMapper modelMapper;

    private List<TransactionDto> parseTransactions(@NotNull Page<TransactionEntity> transactions, String notFoundMessage) {
        if (transactions.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.TRANSACTIONS_NOT_FOUND, notFoundMessage);

        return transactions
                .stream()
                .map(payment -> modelMapper.map(payment, TransactionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public String generateUniqueRefKey(TransactionEntity transactionEntity) {
        return transactionEntity.getMobileMoney() + "-" + transactionEntity.getTransactionRef();
    }

    @Override
    public TransactionDto saveTransaction(@NotNull TransactionEntity transactionEntity) {
        transactionEntity.setUniqueRefKey(generateUniqueRefKey(transactionEntity));
        TransactionEntity saveTransaction = transactionRepository.save(transactionEntity);
        return modelMapper.map(saveTransaction, TransactionDto.class);
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize) {
        Page<TransactionEntity> transactions  = transactionRepository.findAll(Helpers.buildPageable(pageNumber,pageSize));
        return parseTransactions(transactions,String.format("Transactions not found with page number [%s] and page size[%s]",pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize, BigDecimal amount) {
        return parseTransactions(transactionRepository.findAllByAmount(amount,Helpers.buildPageable(pageNumber,pageSize)),
                String.format("Transactions for this amount (%s) and within the range with page number(%s) and page size(%s) not found",amount,pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize,ConditionOperator conditionOperator, BigDecimal conditionalAmount) {
        Page<TransactionEntity>  transactions;
        switch(conditionOperator){
            case GREATER_THAN->transactions= transactionRepository.findAllByAmountGreaterThan(conditionalAmount,Helpers.buildPageable(pageNumber,pageSize));
            case GREATER_THAN_OR_EQUAL_TO->transactions= transactionRepository.findAllByAmountGreaterThanEqual(conditionalAmount,Helpers.buildPageable(pageNumber,pageSize));
            case LESS_THAN->transactions= transactionRepository.findAllByAmountLessThan(conditionalAmount,Helpers.buildPageable(pageNumber,pageSize));
            case LESS_THAN_OR_EQUAL_TO->transactions= transactionRepository.findAllByAmountLessThanEqual(conditionalAmount,Helpers.buildPageable(pageNumber,pageSize));
            default->transactions= null;
        }
        return parseTransactions(transactions,String.format("Transactions not found with the conditional operator(%s) with page number(%s) and page size(%s)",conditionOperator.name(),pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize, TransactionStatus transactionStatus) {
        return parseTransactions(transactionRepository.findAllByTransactionStatus(transactionStatus,Helpers.buildPageable(pageNumber,pageSize)),
                String.format("Transactions not found for status(%s) with page number(%s) and page size(%s)",transactionStatus,pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize, MobileMoney mobileMoney) {
        return parseTransactions(transactionRepository.findAllByMobileMoney(mobileMoney,Helpers.buildPageable(pageNumber,pageSize)),
                String.format("Transactions not found for mobile money(%s) with page number(%s) and page size(%s)",mobileMoney,pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize, TransactionType transactionType) {
        return parseTransactions(transactionRepository.findAllByTransactionType(transactionType,Helpers.buildPageable(pageNumber,pageSize)),
                String.format("Transactions not found for transaction type(%s) with page number(%s) and page size(%s)",transactionType,pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize, String transactionRef) {
        return parseTransactions(transactionRepository.findAllByTransactionRef(transactionRef,Helpers.buildPageable(pageNumber,pageSize)),
                String.format("Transactions not found for transaction ref(%s) with page number(%s) and page size(%s)",transactionRef,pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize, String transactionRef, MobileMoney mobileMoney) {
        return parseTransactions(transactionRepository.findAllByTransactionRefAndMobileMoney(transactionRef,mobileMoney,Helpers.buildPageable(pageNumber,pageSize)),
                String.format("Transactions not found for transaction ref(%s) and mobile money(%s) with page number(%s) and page size(%s)",transactionRef,mobileMoney,pageNumber,pageSize));
    }

    @Override
    public List<TransactionDto> getTransactions(int pageNumber, int pageSize, MobileMoney mobileMoney, String mobileMoneyRef) {
        return parseTransactions(transactionRepository.findAllByMobileMoneyRefAndMobileMoney(mobileMoneyRef,mobileMoney,Helpers.buildPageable(pageNumber,pageSize)),
                String.format("Transactions not found for mobile money ref(%s) and mobile money(%s) with page number(%s) and page size(%s)",mobileMoneyRef,mobileMoney,pageNumber,pageSize));
    }
}
