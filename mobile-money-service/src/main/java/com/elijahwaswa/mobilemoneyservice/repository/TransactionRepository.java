package com.elijahwaswa.mobilemoneyservice.repository;

import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.TransactionStatus;
import com.elijahwaswa.basedomains.enums.TransactionType;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    TransactionEntity findByTransactionRef(String transactionRef);
    TransactionEntity findByMerchantRequestIdAndCheckoutRequestId(String merchantRequestId,String checkoutRequestId);

    Page<TransactionEntity> findAllByAmount(BigDecimal amount, Pageable pageable);

    Page<TransactionEntity> findAllByAmountGreaterThan(BigDecimal amount, Pageable pageable);

    Page<TransactionEntity> findAllByAmountGreaterThanEqual(BigDecimal amount, Pageable pageable);

    Page<TransactionEntity> findAllByAmountLessThan(BigDecimal amount, Pageable pageable);

    Page<TransactionEntity> findAllByAmountLessThanEqual(BigDecimal amount, Pageable pageable);

    Page<TransactionEntity> findAllByTransactionStatus(TransactionStatus transactionStatus, Pageable pageable);

    Page<TransactionEntity> findAllByMobileMoney(MobileMoney mobileMoney, Pageable pageable);

    Page<TransactionEntity> findAllByTransactionType(TransactionType transactionType, Pageable pageable);

    Page<TransactionEntity> findAllByTransactionRef(String transactionRef, Pageable pageable);

    Page<TransactionEntity> findAllByTransactionRefAndMobileMoney(String transactionRef, MobileMoney mobileMoney, Pageable pageable);

    Page<TransactionEntity> findAllByMobileMoneyRefAndMobileMoney(String mobileMoneyRef, MobileMoney mobileMoney, Pageable pageable);
}
