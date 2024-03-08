package com.elijahwaswa.mobilemoneyservice.service.impl;

import base.Helpers;
import com.elijahwaswa.basedomains.enums.*;
import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.entity.Reference;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;
import com.elijahwaswa.mobilemoneyservice.exception.InternalErrorException;
import com.elijahwaswa.mobilemoneyservice.service.MoneyService;
import com.elijahwaswa.mobilemoneyservice.service.TransactionService;
import com.elijahwaswa.mobilemoneyservice.service.reference.ReferenceService;
import mpesa.MpesaClient;
import mpesa.MpesaResponse;
import mpesa.dto.MpesaRequestDto;
import mpesa.stk.Item;
import mpesa.util.Environment;
import mpesa.util.ResponseParserType;
import mpesa.util.STKTransactionType;
import mpesa.util.TrxCodeType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static base.Helpers.MPESA_TIMESTAMP_FORMAT;

@Service
public class MoneyServiceImpl implements MoneyService {

    private final ModelMapper modelMapper;
    private final ReferenceService referenceService;
    private final TransactionService transactionService;
    private final String activeProfile;
    private final String baseUrl;
    private final String relativeCallbackUrl;
    private final String consumerKey;
    private final String consumerSecret;
    private final String mpesaPassKey;
    private final String mpesaInitiatorName;
    private final String mpesaInitiatorPassword;
    private final String payBillShortcode;
    private final String buyGoodsShortcode;

    public MoneyServiceImpl(
            ModelMapper modelMapper,
            ReferenceService referenceService,
            TransactionService transactionService,
            @Value("${spring.profiles.active}") String activeProfile,
            @Value("${base.url}") String baseUrl,
            @Value("${mpesa.relative_callback_url}") String relativeCallbackUrl,
            @Value("${mpesa.consumer_key}") String consumerKey,
            @Value("${mpesa.consumer_secret}") String consumerSecret,
            @Value("${mpesa.pass_key}") String mpesaPassKey,
            @Value("${mpesa.initiator_name}") String mpesaInitiatorName,
            @Value("${mpesa.initiator_password}") String mpesaInitiatorPassword,
            @Value("${mpesa.pay_bill}") String payBillShortcode,
            @Value("${mpesa.buy_goods}") String buyGoodsShortcode) {
        this.modelMapper = modelMapper;
        this.referenceService = referenceService;
        this.transactionService = transactionService;
        this.activeProfile = activeProfile;
        this.baseUrl = baseUrl;
        this.relativeCallbackUrl = relativeCallbackUrl;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.mpesaPassKey = mpesaPassKey;
        this.mpesaInitiatorName = mpesaInitiatorName;
        this.mpesaInitiatorPassword = mpesaInitiatorPassword;
        this.payBillShortcode = payBillShortcode;
        this.buyGoodsShortcode = buyGoodsShortcode;
    }

    /**
     * Determines the current environment the mobile money service is actively running on
     *
     * @return Environment
     */
    private Environment getEnvironment() {
        return !activeProfile.equals(AppProfile.PROD.getValue()) ? Environment.DEVELOPMENT : Environment.PRODUCTION;
    }

    /**
     * Initializes MpesaClient
     * @param setInitiatorCredentials Whether to include initiator credentials
     * @return MpesaClient
     */
    private MpesaClient initializeMpesaClient(boolean setInitiatorCredentials) {
        MpesaClient mpesaClient = new MpesaClient()
                .environment(getEnvironment())
                .passKey(mpesaPassKey)
                .consumerKey(consumerKey)
                .consumerSecret(consumerSecret);

        if (setInitiatorCredentials) {
            mpesaClient
                    .initiatorName(mpesaInitiatorName)
                    .initiatorPassword(mpesaInitiatorPassword);
        }
        return mpesaClient;
    }

    /**
     * Determines whether stk was successful, fetches the record from db and prepares some fields for update.
     * @param mpesaResponse Response from http request
     * @return TransactionEntity
     */
    private TransactionEntity parseMpesaC2BStkResponse(MpesaResponse mpesaResponse) {
        //fetch record from db
        TransactionEntity transactionEntity = mpesaResponse.getBody() != null
                && mpesaResponse.getBody().getStkCallback() != null
                && mpesaResponse.getBody().getStkCallback().getMerchantRequestID() != null
                && mpesaResponse.getBody().getStkCallback().getCheckoutRequestID() != null ?
                transactionService.getTransaction(mpesaResponse.getBody().getStkCallback().getMerchantRequestID(), mpesaResponse.getBody().getStkCallback().getCheckoutRequestID()) : null;

        if (mpesaResponse.getBody() != null
                && mpesaResponse.getBody().getStkCallback() != null
                && mpesaResponse.getBody().getStkCallback().getCallbackMetadata() != null
                && mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems() != null
                && !mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems().isEmpty()) {
            //extract fields
            for (Item item : mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems()) {
                if (item.getName().equals("MpesaReceiptNumber")) {
                    assert transactionEntity != null;
                    transactionEntity.setMobileMoneyRef(item.getValue());
                } else if (item.getName().equals("TransactionDate")) {
                    assert transactionEntity != null;
                    transactionEntity.setMobileMoneyTransactionDate(LocalDateTime.parse(item.getValue(), DateTimeFormatter.ofPattern(MPESA_TIMESTAMP_FORMAT)));
                }
            }
        }
        return transactionEntity;
    }

    /**
     * Validates whether the request was successful and updates the record in db
     * @param mpesaResponse http response
     * @param responseParserType response type
     * @return TransactionDto
     */
    @Override
    public TransactionDto validateResponse(MpesaResponse mpesaResponse, ResponseParserType responseParserType) {
        //validate if successful
        new MpesaClient()
                .responseParser(mpesaResponse, responseParserType);

        //db data POJO
        TransactionEntity transactionEntity = null;

        //get the transaction entity
        switch (responseParserType) {
            case ResponseParserType.C2B_STK -> transactionEntity = parseMpesaC2BStkResponse(mpesaResponse);
        }

        //if not transaction entity,return early
        if (transactionEntity == null) return null;

        //update status
        if (transactionEntity != null) {
            transactionEntity.setTransactionStatus(mpesaResponse.isInternalStatus() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        }

        return transactionService.updateTransaction(transactionEntity);
    }

    @Override
    public TransactionDto initStkPush(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, STKTransactionType stkTransactionType, double amount, long phoneNumber, String briefDescription) {
        if (mobileMoney.equals(MobileMoney.AIRTEL)) {
            return null;
        }

        Reference reference = referenceService.generateReference();
        if (reference == null) {
            throw new InternalErrorException(ErrorCode.REF_GENERATION_FAILED, "Failed to generate reference number");
        }
        String referenceId = reference.getReference();
        int businessShortcode = Integer.parseInt(mobileMoneyShortcodeType.equals(MobileMoneyShortcodeType.BUY_GOODS) ? buyGoodsShortcode : payBillShortcode);
        String callbackUrl = Helpers.buildUrl(null, baseUrl, relativeCallbackUrl).toString();

        //mpesa request payload
        MpesaRequestDto mpesaRequest = new MpesaRequestDto();
        mpesaRequest.setStkTransactionType(stkTransactionType);
        mpesaRequest.setBusinessShortCode(businessShortcode);
        mpesaRequest.setAmount(amount);
        mpesaRequest.setPhoneNumber(phoneNumber);
        mpesaRequest.setCallbackURL(callbackUrl);
        mpesaRequest.setAccountReference(referenceId);
        mpesaRequest.setTransactionDesc(briefDescription);

        TransactionStatus transactionStatus = TransactionStatus.REQUEST_PENDING;
        String errorMessage = null, merchantRequestId = null, checkoutRequestId = null;
        try {
            //db payload
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionStatus(transactionStatus);
            transactionEntity.setMobileMoney(mobileMoney);
            transactionEntity.setTransactionType(TransactionType.STK_SEND);
            transactionEntity.setStkTransactionType(stkTransactionType);
            transactionEntity.setCreditBusinessShortCode(businessShortcode);
            transactionEntity.setPhoneNumber(phoneNumber);
            transactionEntity.setAmount(new BigDecimal(amount));
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setCallbackUrl(callbackUrl);
            transactionEntity.setDescription(briefDescription);
            transactionService.saveTransaction(transactionEntity);

            MpesaResponse mpesaResponse = initializeMpesaClient(false)
                    .mpesaRequestDto(mpesaRequest)
                    .stkSend();
            if (mpesaResponse.isInternalStatus()) {
                transactionStatus = TransactionStatus.REQUEST_ACCEPTED;
                merchantRequestId = mpesaResponse.getMerchantRequestID();
                checkoutRequestId = mpesaResponse.getCheckoutRequestID();
            } else {
                transactionStatus = TransactionStatus.REQUEST_FAILED;
                errorMessage = mpesaResponse.getErrorMessage();
            }
        } catch (Exception e) {
            throw new InternalErrorException(ErrorCode.ERROR, e.getMessage());
        }


        //update
        TransactionEntity transactionEntity = transactionService.getTransaction(referenceId);
        transactionEntity.setTransactionStatus(transactionStatus);
        if (errorMessage != null) {
            transactionEntity.setMessage(errorMessage);
        }
        if (merchantRequestId != null) {
            transactionEntity.setMerchantRequestId(merchantRequestId);
        }
        if (checkoutRequestId != null) {
            transactionEntity.setCheckoutRequestId(checkoutRequestId);
        }

        return transactionService.updateTransaction(transactionEntity);
    }

    @Override
    public TransactionDto checkStkStatus(MobileMoney mobileMoney, String transactionRef) {
        //fetch the record from db
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionRef);
        if (transactionEntity.getTransactionStatus() != TransactionStatus.REQUEST_ACCEPTED) {
            return modelMapper.map(transactionEntity, TransactionDto.class);
        }

        //mpesa request payload
        MpesaRequestDto mpesaRequest = new MpesaRequestDto();
        mpesaRequest.setBusinessShortCode(transactionEntity.getCreditBusinessShortCode());
        mpesaRequest.setCheckoutRequestID(transactionEntity.getCheckoutRequestId());

        MpesaResponse mpesaResponse;
        try {
            mpesaResponse = initializeMpesaClient(false)
                    .mpesaRequestDto(mpesaRequest)
                    .stkQuery();

        } catch (Exception e) {
            throw new InternalErrorException(ErrorCode.ERROR, e.getMessage());
        }

        //update the transaction status
        if (mpesaResponse.isInternalStatus()) {
            transactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);
        }
        if (mpesaResponse.getErrorMessage() != null) {
            transactionEntity.setMessage(mpesaResponse.getErrorMessage());
        }
        return transactionService.updateTransaction(transactionEntity);
    }

    @Override
    public TransactionDto registerUrls(MobileMoney mobileMoney, int businessShortCode) {
        return null;
    }

    @Override
    public TransactionDto businessToBusinessPayment(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, long creditPhoneNumber, String remarks) {
        return null;
    }

    @Override
    public TransactionDto businessToBusinessPaymentStkPush(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, String creditBusinessName) {
        return null;
    }

    @Override
    public TransactionDto businessToCustomerDisbursement(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, long phoneNumber, double amount, String remarks) {
        return null;
    }

    @Override
    public TransactionDto remitTax(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, String remarks) {
        return null;
    }

    @Override
    public TransactionDto generateQrCode(MobileMoney mobileMoney, TrxCodeType trxCodeType, double amount) {
        return null;
    }
}
