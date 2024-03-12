package com.elijahwaswa.mobilemoneyservice.entity;

import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.TransactionStatus;
import com.elijahwaswa.basedomains.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import mpesa.util.STKTransactionType;
import mpesa.util.TrxCodeType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name="transactions")
@EntityListeners(AuditingEntityListener.class)
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(unique = true)
    private String uniqueRefKey;

    private TransactionStatus transactionStatus;
    private MobileMoney mobileMoney;
    private TransactionType transactionType;
    private STKTransactionType stkTransactionType;
    private int creditBusinessShortCode;//receiving
    private int debitBusinessShortCode;//sending
    private String phoneNumber;
    private BigDecimal amount;
    private String transactionRef;
    private String mobileMoneyRef;
    private int responseCode;
    private int resultCode;
    private String conversationId;
    private String convoId;
    private String merchantRequestId;
    private String checkoutRequestId;
    private String requestId;
    private String taxPrn;
    private String callbackUrl;
    private String timeoutUrl;
    private String validationUrl;
    private String confirmationUrl;
    private String firstName;
    private String middleName;
    private String lastName;
    private String description;
    private String message;
    private TrxCodeType trxCodeType;
    private LocalDateTime mobileMoneyTransactionDate;
}
