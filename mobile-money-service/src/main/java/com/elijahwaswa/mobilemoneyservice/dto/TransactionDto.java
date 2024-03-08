package com.elijahwaswa.mobilemoneyservice.dto;

import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.TransactionStatus;
import com.elijahwaswa.basedomains.enums.TransactionType;
import lombok.Data;
import mpesa.util.STKTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionDto {
    private UUID id;
    private TransactionStatus transactionStatus;
    private MobileMoney mobileMoney;
    private TransactionType transactionType;
    private STKTransactionType stkTransactionType;
    private int creditBusinessShortCode;//receiving
    private int debitBusinessShortCode;//sending
    private BigDecimal amount;
    private String transactionRef;
    private String mobileMoneyRef;
    private int responseCode;
    private int resultCode;
    private String conversationId;
    private String merchantRequestId;
    private String checkoutRequestId;
    private String name;
    private String description;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
