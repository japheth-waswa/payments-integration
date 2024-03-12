package com.elijahwaswa.mobilemoneyservice.service.impl;

import base.Helpers;
import com.elijahwaswa.basedomains.enums.*;
import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.entity.Reference;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;
import com.elijahwaswa.mobilemoneyservice.exception.InternalErrorException;
import com.elijahwaswa.mobilemoneyservice.exception.ResourceNotFoundException;
import com.elijahwaswa.mobilemoneyservice.service.MoneyService;
import com.elijahwaswa.mobilemoneyservice.service.TransactionService;
import com.elijahwaswa.mobilemoneyservice.service.reference.ReferenceService;
import lombok.RequiredArgsConstructor;
import mpesa.MpesaClient;
import mpesa.MpesaResponse;
import mpesa.dto.MpesaRequestDto;
import mpesa.stk.Item;
import mpesa.util.*;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static base.Helpers.MPESA_TIMESTAMP_FORMAT;

@Service
@RequiredArgsConstructor
public class MoneyServiceImpl implements MoneyService {

    public static final int ROUNDING_DECIMALS = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final ModelMapper modelMapper;
    private final ReferenceService referenceService;
    private final TransactionService transactionService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${mpesa.relative_callback_url}")
    private String relativeCallbackUrl;

    @Value("${mpesa.relative_timeout_url}")
    private String relativeTimeoutUrl;

    @Value("${mpesa.relative_validation_url}")
    private String relativeValidationUrl;

    @Value("${mpesa.relative_confirmation_url}")
    private String relativeConfirmationUrl;

    @Value("${mpesa.consumer_key}")
    private String consumerKey;

    @Value("${mpesa.consumer_secret}")
    private String consumerSecret;

    @Value("${mpesa.pass_key}")
    private String mpesaPassKey;

    @Value("${mpesa.initiator_name}")
    private String mpesaInitiatorName;

    @Value("${mpesa.initiator_password}")
    private String mpesaInitiatorPassword;

    @Value("${mpesa.pay_bill}")
    private String payBillShortcode;

    @Value("${mpesa.pay_bill.name}")
    private String payBillShortcodeName;

    @Value("${mpesa.buy_goods}")
    private String buyGoodsShortcode;

    @Value("${mpesa.buy_goods.name}")
    private String buyGoodsShortcodeName;

    @Value("${mpesa.agent_till}")
    private String agentTill;

    @Value("${mpesa.agent_till.name}")
    private String agentTillName;

    @Value("${mpesa.phone_number}")
    private String mpesaPhoneNumber;

    @Value("${kra.tax_prn}")
    private String kraTaxPrn;

    /**
     * Determines the current environment the mobile money service is actively running on
     *
     * @return Environment
     */
    private Environment getEnvironment() {
        return !activeProfile.equals(AppProfile.PROD.getValue()) ? Environment.DEVELOPMENT : Environment.PRODUCTION;
    }

    /**
     * intialize mpesa client without initiator credentials
     *
     * @return MpesaClient
     */
    private MpesaClient initializeMpesaClient() {
        return new MpesaClient()
                .environment(getEnvironment())
                .passKey(mpesaPassKey)
                .consumerKey(consumerKey)
                .consumerSecret(consumerSecret);
    }

    /**
     * initialize mpesa client with initiator credentials
     *
     * @return MpesaClient
     */
    private MpesaClient initializeMpesaClientWithCredentials() {
        return new MpesaClient()
                .environment(getEnvironment())
                .passKey(mpesaPassKey)
                .consumerKey(consumerKey)
                .consumerSecret(consumerSecret)
                .initiatorName(mpesaInitiatorName)
                .initiatorPassword(mpesaInitiatorPassword);
    }

    /**
     * Get business shortcode
     *
     * @param mobileMoneyShortcodeType
     * @return int
     */
    private int getBusinessShortcode(MobileMoneyShortcodeType mobileMoneyShortcodeType) {
        return switch (mobileMoneyShortcodeType) {
            case BUY_GOODS -> Integer.parseInt(buyGoodsShortcode);
            case PAY_BILL -> Integer.parseInt(payBillShortcode);
            case AGENT_TILL -> Integer.parseInt(agentTill);
        };
    }

    /**
     * Get business shortcode name
     *
     * @param mobileMoneyShortcodeType
     * @return String
     */
    private String getBusinessShortcodeName(MobileMoneyShortcodeType mobileMoneyShortcodeType) {
        return switch (mobileMoneyShortcodeType) {
            case BUY_GOODS -> buyGoodsShortcodeName;
            case PAY_BILL -> payBillShortcodeName;
            case AGENT_TILL -> agentTillName;
        };
    }

    /**
     * Determines whether stk was successful, fetches the record from db and prepares some fields for update.
     *
     * @param mpesaResponse Response from http request
     * @return TransactionEntity
     */
    private TransactionEntity parseMpesaC2BStkResponse(MpesaResponse mpesaResponse) {
        //fetch record from db
        TransactionEntity transactionEntity = mpesaResponse.getBody() != null
                && mpesaResponse.getBody().getStkCallback() != null
                && mpesaResponse.getBody().getStkCallback().getMerchantRequestID() != null
                && mpesaResponse.getBody().getStkCallback().getCheckoutRequestID() != null ?
                transactionService.getTransaction(
                        mpesaResponse.getBody().getStkCallback().getMerchantRequestID(),
                        mpesaResponse.getBody().getStkCallback().getCheckoutRequestID()
                )
                : null;

        if (transactionEntity == null) return null;

        if (mpesaResponse.getBody() != null
                && mpesaResponse.getBody().getStkCallback() != null
                && mpesaResponse.getBody().getStkCallback().getCallbackMetadata() != null
                && mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems() != null
                && !mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems().isEmpty()) {
            //extract fields
            for (Item item : mpesaResponse.getBody().getStkCallback().getCallbackMetadata().getItems()) {
                if (item.getName().equals("MpesaReceiptNumber")) {
                    transactionEntity.setMobileMoneyRef(item.getValue());
                } else if (item.getName().equals("TransactionDate")) {
                    transactionEntity.setMobileMoneyTransactionDate(LocalDateTime.parse(item.getValue(), DateTimeFormatter.ofPattern(MPESA_TIMESTAMP_FORMAT)));
                }
            }
        }
        return transactionEntity;
    }

    /**
     * Determines whether B2B or B2C was successful and prepares some fields for update.
     *
     * @param mpesaResponse Response from http request
     * @return TransactionEntity
     */
    private TransactionEntity parseMpesaResponseGeneric(MpesaResponse mpesaResponse) {
        if (mpesaResponse.getResult() == null) return null;

        String mobileMoneyRef = mpesaResponse.getResult().getTransactionId();
        String originatorConversationId = mpesaResponse.getResult().getOriginatorConversationId();
        String conversationId = mpesaResponse.getResult().getConversationId();

        TransactionEntity transactionEntity = transactionService.getTransactionByConversationId(originatorConversationId, conversationId);
        if (transactionEntity == null) return null;

        if (mobileMoneyRef != null) transactionEntity.setMobileMoneyRef(mobileMoneyRef);

        return transactionEntity;
    }

    /**
     * Determines whether B2B STK was successful and prepares some fields for update.
     *
     * @param mpesaResponse
     * @return TransactionEntity
     */
    private TransactionEntity parseMpesaB2BStkResponse(MpesaResponse mpesaResponse) {
        String requestId = mpesaResponse.getRequestId();
        if (requestId == null) return null;
        String mobileMoneyRef = mpesaResponse.getTransactionId();

        TransactionEntity transactionEntity = transactionService.getTransactionByRequestId(requestId);
        if (transactionEntity == null) return null;

        if (mobileMoneyRef != null) transactionEntity.setMobileMoneyRef(mobileMoneyRef);
        return transactionEntity;
    }

    /**
     * Get transaction entity
     *
     * @param mpesaResponse
     * @param responseParserType
     * @return TransactionEntity
     */
    private TransactionEntity getTransactionEntity(MpesaResponse mpesaResponse, ResponseParserType responseParserType) {
        return switch (responseParserType) {
            case C2B_STK -> parseMpesaC2BStkResponse(mpesaResponse);
            case B2B_PAYMENT, B2C, TAX_REMITTANCE -> parseMpesaResponseGeneric(mpesaResponse);
            case B2B_STK -> parseMpesaB2BStkResponse(mpesaResponse);
//            case TAX_REMITTANCE -> null;
        };
    }

    /**
     * Generates a reference number
     *
     * @return String
     */
    private String generateReferenceId() {
        Reference reference = referenceService.generateReference();
        if (reference == null) {
            throw new InternalErrorException(ErrorCode.REF_GENERATION_FAILED, "Failed to generate reference number");
        }
        return reference.getReference();
    }

    /**
     * Validates whether the request was successful and updates the record in db
     *
     * @param mpesaResponse      http response
     * @param responseParserType response type
     * @return TransactionDto
     */
    @Override
    public TransactionDto validateResponse(MpesaResponse mpesaResponse, ResponseParserType responseParserType) {
        //validate if successful
        new MpesaClient().responseParser(mpesaResponse, responseParserType);

        //db data POJO
        TransactionEntity transactionEntity = getTransactionEntity(mpesaResponse, responseParserType);

        //if transaction entity null,return early
        if (transactionEntity == null) return null;

        //update status
        transactionEntity.setTransactionStatus(mpesaResponse.isInternalStatus() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);

        return transactionService.updateTransaction(transactionEntity);
    }

    /**
     * Initiates stk push
     *
     * @param mobileMoney
     * @param mobileMoneyShortcodeType
     * @param stkTransactionType
     * @param amount
     * @param phoneNumber
     * @param briefDescription
     * @return TransactionDto
     */
    @Override
    public TransactionDto initStkPush(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, STKTransactionType stkTransactionType, double amount, long phoneNumber, String briefDescription) {
        if (mobileMoney.equals(MobileMoney.AIRTEL)) {
            return null;
        }

        String referenceId = generateReferenceId();
        int businessShortcode = getBusinessShortcode(mobileMoneyShortcodeType);
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
            transactionEntity.setPhoneNumber(String.valueOf(phoneNumber));
            transactionEntity.setAmount(getAmount(amount));
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setCallbackUrl(callbackUrl);
            transactionEntity.setDescription(briefDescription);
            transactionService.saveTransaction(transactionEntity);

            MpesaResponse mpesaResponse = initializeMpesaClient()
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


    /**
     * Check stk status
     *
     * @param mobileMoney
     * @param transactionRef
     * @return TransactionDto
     */
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
            mpesaResponse = initializeMpesaClient()
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

    /**
     * Register validation and confirmation urls
     *
     * @param mobileMoney
     * @param mobileMoneyShortcodeType
     * @param registerURLResponseType
     * @return TransactionDto
     */
    @Override
    public TransactionDto registerUrls(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, RegisterURLResponseType registerURLResponseType) {
        String referenceId = generateReferenceId();
        String confirmationUrl = Helpers.buildUrl(null, baseUrl, relativeConfirmationUrl).toString();
        String validationUrl = Helpers.buildUrl(null, baseUrl, relativeValidationUrl).toString();

        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setBusinessShortCode(getBusinessShortcode(mobileMoneyShortcodeType));
        mpesaRequestDto.setRegisterURLResponseType(registerURLResponseType);
        mpesaRequestDto.setConfirmationURL(confirmationUrl);
        mpesaRequestDto.setValidationURL(validationUrl);

        String errorMessage = null;
        TransactionStatus transactionStatus = TransactionStatus.REQUEST_PENDING;
        MpesaResponse mpesaResponse;
        try {
            //store the record in db using TransactionEntity
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionStatus(transactionStatus);
            transactionEntity.setMobileMoney(mobileMoney);
            transactionEntity.setTransactionType(TransactionType.C2B_REGISTER_URL);
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setConfirmationUrl(confirmationUrl);
            transactionEntity.setValidationUrl(validationUrl);
            transactionService.saveTransaction(transactionEntity);

            //make request for registration
            mpesaResponse = initializeMpesaClient()
                    .mpesaRequestDto(mpesaRequestDto)
                    .C2BRegisterURL();
            if (mpesaResponse.isInternalStatus()) {
                transactionStatus = TransactionStatus.SUCCESS;
            } else {
                transactionStatus = TransactionStatus.FAILED;
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

        return transactionService.updateTransaction(transactionEntity);
    }

    /**
     * Validates pay bill payment from validation url
     *
     * @param mpesaResponse
     * @return MpesaResponse
     */
    private MpesaResponse validatePayBillPayment(MpesaResponse mpesaResponse) {
        String billRefNumber = mpesaResponse.getBillRefNumber();
        double transAmount = mpesaResponse.getTransAmount();

        //get the record from db
        TransactionEntity transactionEntity;
        try {
            transactionEntity = transactionService.getTransaction(billRefNumber);
        } catch (ResourceNotFoundException e) {
            return new MpesaClient().generateValidationResponse(ResponseStatus.INVALID_ACCOUNT_NUMBER, ResultDesc.REJECTED, null);
        }

        //check phone number matches
        if (!com.elijahwaswa.basedomains.utils.Helpers.phoneNumbersMatch(transactionEntity.getPhoneNumber(), mpesaResponse.getPhoneNumber())) {
            return new MpesaClient().generateValidationResponse(ResponseStatus.INVALID_MSISDN, ResultDesc.REJECTED, null);
        }

        //check amount matches
        BigDecimal amount = transactionEntity.getAmount();
        if (transactionEntity.getTransactionStatus() == TransactionStatus.SUCCESS) {
            return new MpesaClient().generateValidationResponse(ResponseStatus.OTHER_ERROR, ResultDesc.REJECTED, null);
        }
        if (amount == null || !amount.setScale(ROUNDING_DECIMALS, ROUNDING_MODE).equals(getAmount(transAmount))) {
            return new MpesaClient().generateValidationResponse(ResponseStatus.INVALID_AMOUNT, ResultDesc.REJECTED, null);
        }

        return new MpesaClient().generateValidationResponse(ResponseStatus.SUCCESS, ResultDesc.ACCEPTED, null);
    }

    /**
     * Parse mobile money response C2B for either validation or confirmation.
     *
     * @param mobileMoneyResponseType
     * @param mpesaResponse
     * @return MpesaResponse
     */
    @Override
    public MpesaResponse parseMobileMoneyResponse(MobileMoneyResponseType mobileMoneyResponseType, MpesaResponse mpesaResponse) {
        String transactionType = mpesaResponse.getTransactionType();
        String billRefNumber = mpesaResponse.getBillRefNumber();
        if (mobileMoneyResponseType.equals(MobileMoneyResponseType.VALIDATION) && !transactionType.equals("Pay Bill")) {
            //if not pay bill then accept,validate the transaction.
            return new MpesaClient().generateValidationResponse(ResponseStatus.SUCCESS, ResultDesc.ACCEPTED, null);
        }

        if (mobileMoneyResponseType.equals(MobileMoneyResponseType.VALIDATION)) {
            //if pay bill then validate the transaction.
            return validatePayBillPayment(mpesaResponse);
        }

        //confirmation MobileMoneyResponseType.CONFIRMATION
        TransactionEntity transactionEntity = null;
        try {
            transactionEntity = transactionService.getTransaction(billRefNumber);
        } catch (ResourceNotFoundException e) {
        }

        if (transactionEntity != null) {
            //update the record.
            transactionEntity.setMobileMoneyRef(mpesaResponse.getTransID());
            transactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionEntity.setMobileMoneyTransactionDate(mpesaResponse.getTransTime());
            transactionEntity.setFirstName(mpesaResponse.getFirstName());
            transactionEntity.setMiddleName(mpesaResponse.getMiddleName());
            transactionEntity.setLastName(mpesaResponse.getLastName());
            transactionService.updateTransaction(transactionEntity);
        } else {
            //create the record
            transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionRef(billRefNumber != null ? billRefNumber : generateReferenceId());
            transactionEntity.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionEntity.setTransactionType(TransactionType.C2B_PAYMENT);
            transactionEntity.setMobileMoney(MobileMoney.MPESA);
            transactionEntity.setMobileMoneyRef(mpesaResponse.getTransID());
            transactionEntity.setMobileMoneyTransactionDate(mpesaResponse.getTransTime());
            transactionEntity.setAmount(BigDecimal.valueOf(mpesaResponse.getTransAmount()).setScale(ROUNDING_DECIMALS, ROUNDING_MODE));
            transactionEntity.setCreditBusinessShortCode(mpesaResponse.getBusinessShortCode());
            transactionEntity.setPhoneNumber(mpesaResponse.getPhoneNumber());
            transactionEntity.setFirstName(mpesaResponse.getFirstName());
            transactionEntity.setMiddleName(mpesaResponse.getMiddleName());
            transactionEntity.setLastName(mpesaResponse.getLastName());
            transactionService.saveTransaction(transactionEntity);
        }
        //confirmation acknowledgement
        return new MpesaClient().generateAcknowledgmentResponse();
    }

    /**
     * Get amount
     *
     * @param amount
     * @return BigDecimal
     */
    @NotNull
    private static BigDecimal getAmount(double amount) {
        return new BigDecimal(amount).setScale(ROUNDING_DECIMALS, ROUNDING_MODE);
    }

    /**
     * Business to business payment
     *
     * @param mobileMoney
     * @param mobileMoneyShortcodeType
     * @param amount
     * @param creditBusinessShortCode
     * @param creditPhoneNumber
     * @param remarks
     * @return TransactionDto
     */
    @Override
    public TransactionDto businessToBusinessPayment(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, long creditPhoneNumber, String remarks) {
        if (mobileMoney.equals(MobileMoney.AIRTEL)) {
            return null;
        }

        int debitBusinessShortCode = getBusinessShortcode(mobileMoneyShortcodeType);
        String referenceId = generateReferenceId();
        String callbackUrl = Helpers.buildUrl(null, baseUrl, relativeCallbackUrl).toString();
        String timeoutUrl = Helpers.buildUrl(null, baseUrl, relativeTimeoutUrl).toString();

        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setMpesaRequestType(mobileMoneyShortcodeType.equals(MobileMoneyShortcodeType.PAY_BILL) ? MpesaRequestType.B2B_PAY_BILL : MpesaRequestType.B2B_BUY_GOODS);
        mpesaRequestDto.setAmount(amount);
        mpesaRequestDto.setPartyA((long) debitBusinessShortCode);
        mpesaRequestDto.setPartyB((long) creditBusinessShortCode);
        mpesaRequestDto.setAccountReference(referenceId);
        mpesaRequestDto.setPhoneNumber(creditPhoneNumber);
        mpesaRequestDto.setRemarks(remarks);
        mpesaRequestDto.setQueueTimeOutURL(timeoutUrl);
        mpesaRequestDto.setResultURL(callbackUrl);

        TransactionStatus transactionStatus = TransactionStatus.REQUEST_PENDING;
        String message, originatorConversationId = null, conversationId = null;
        try {
            //db payload
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionStatus(transactionStatus);
            transactionEntity.setMobileMoney(mobileMoney);
            transactionEntity.setTransactionType(mobileMoneyShortcodeType.equals(MobileMoneyShortcodeType.PAY_BILL) ? TransactionType.B2B_PAY_BILL : TransactionType.B2B_BUY_GOODS);
            transactionEntity.setCreditBusinessShortCode(creditBusinessShortCode);
            transactionEntity.setDebitBusinessShortCode(debitBusinessShortCode);
            transactionEntity.setPhoneNumber(String.valueOf(creditPhoneNumber));
            transactionEntity.setAmount(getAmount(amount));
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setCallbackUrl(callbackUrl);
            transactionEntity.setTimeoutUrl(timeoutUrl);
            transactionEntity.setDescription(remarks);
            transactionService.saveTransaction(transactionEntity);

            MpesaResponse mpesaResponse = initializeMpesaClientWithCredentials()
                    .mpesaRequestDto(mpesaRequestDto)
                    .B2BPayment();
            if (mpesaResponse.isInternalStatus()) {
                transactionStatus = TransactionStatus.REQUEST_ACCEPTED;
                originatorConversationId = mpesaResponse.getOriginatorConversationId();
                conversationId = mpesaResponse.getConversationId();
                message = mpesaResponse.getResponseDescription();
            } else {
                transactionStatus = TransactionStatus.REQUEST_FAILED;
                message = mpesaResponse.getErrorMessage();
            }
        } catch (Exception e) {
            throw new InternalErrorException(ErrorCode.ERROR, e.getMessage());
        }

        //update
        TransactionEntity transactionEntity = transactionService.getTransaction(referenceId);
        transactionEntity.setTransactionStatus(transactionStatus);
        if (message != null) {
            transactionEntity.setMessage(message);
        }
        if (originatorConversationId != null) {
            transactionEntity.setConversationId(originatorConversationId);
        }
        if (conversationId != null) {
            transactionEntity.setConvoId(conversationId);
        }

        return transactionService.updateTransaction(transactionEntity);
    }

    /**
     * Business to business stk payment
     *
     * @param mobileMoney
     * @param mobileMoneyShortcodeType
     * @param amount
     * @param creditBusinessShortCode
     * @param creditBusinessName
     * @return TransactionDto
     */
    @Override
    public TransactionDto businessToBusinessPaymentStkPush(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, String creditBusinessName) {
        if (mobileMoney.equals(MobileMoney.AIRTEL)) {
            return null;
        }

        int debitBusinessShortCode = getBusinessShortcode(mobileMoneyShortcodeType);
        String referenceId = generateReferenceId();
        String requestRefId = UUID.randomUUID().toString();
        String callbackUrl = Helpers.buildUrl(null, baseUrl, relativeCallbackUrl).toString();

        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setSendingPartyShortCode(debitBusinessShortCode);
        mpesaRequestDto.setReceivingPartyShortCode(creditBusinessShortCode);
        mpesaRequestDto.setReceivingPartyName(creditBusinessName);
        mpesaRequestDto.setAmt(amount);
        mpesaRequestDto.setPaymentRef(referenceId);
        mpesaRequestDto.setCallback(callbackUrl);
        mpesaRequestDto.setRequestRefId(requestRefId);

        TransactionStatus transactionStatus = TransactionStatus.REQUEST_PENDING;
        String message = null;
        try {
            //db payload
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionStatus(transactionStatus);
            transactionEntity.setMobileMoney(mobileMoney);
            transactionEntity.setTransactionType(TransactionType.B2B_STK);
            transactionEntity.setCreditBusinessShortCode(creditBusinessShortCode);
            transactionEntity.setFirstName(creditBusinessName);
            transactionEntity.setDebitBusinessShortCode(debitBusinessShortCode);
            transactionEntity.setAmount(getAmount(amount));
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setCallbackUrl(callbackUrl);
            transactionEntity.setRequestId(requestRefId);
            transactionService.saveTransaction(transactionEntity);

            MpesaResponse mpesaResponse = initializeMpesaClient()
                    .mpesaRequestDto(mpesaRequestDto)
                    .B2BStk();
            if (mpesaResponse.isInternalStatus()) {
                transactionStatus = TransactionStatus.REQUEST_ACCEPTED;
            } else {
                transactionStatus = TransactionStatus.REQUEST_FAILED;
                message = mpesaResponse.getErrorMessage();
            }
        } catch (Exception e) {
            throw new InternalErrorException(ErrorCode.ERROR, e.getMessage());
        }

        //update
        TransactionEntity transactionEntity = transactionService.getTransaction(referenceId);
        transactionEntity.setTransactionStatus(transactionStatus);
        if (message != null) {
            transactionEntity.setMessage(message);
        }
        return transactionService.updateTransaction(transactionEntity);
    }

    /**
     * Update transaction record
     *
     * @param referenceId
     * @param transactionStatus
     * @param message
     * @param convoId
     * @param conversationId
     * @return TransactionDto
     */
    private TransactionDto updateTransactionRecord(String referenceId, TransactionStatus transactionStatus, String message, String convoId, String conversationId) {
        TransactionEntity transactionEntityRecord = transactionService.getTransaction(referenceId);
        transactionEntityRecord.setTransactionStatus(transactionStatus);
        if (message != null) {
            transactionEntityRecord.setMessage(message);
        }
        if (convoId != null) {
            transactionEntityRecord.setConvoId(convoId);
        }
        if (conversationId != null) {
            transactionEntityRecord.setConversationId(conversationId);
        }
        return transactionService.updateTransaction(transactionEntityRecord);
    }

    /**
     * Business to customer disbursement
     *
     * @param mobileMoney
     * @param mobileMoneyShortcodeType
     * @param b2CCommandId
     * @param phoneNumber
     * @param amount
     * @param remarks
     * @return TransactionDto
     */
    @Override
    public TransactionDto businessToCustomerDisbursement(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, B2CCommandID b2CCommandId, long phoneNumber, double amount, String remarks) {
        if (mobileMoney.equals(MobileMoney.AIRTEL)) return null;

        int businessShortcode = getBusinessShortcode(mobileMoneyShortcodeType);
        String referenceId = generateReferenceId();
        String conversationId = UUID.randomUUID().toString();
        String callbackUrl = Helpers.buildUrl(null, baseUrl, relativeCallbackUrl).toString();
        String timeoutUrl = Helpers.buildUrl(null, baseUrl, relativeTimeoutUrl).toString();

        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setOriginatorConversationId(conversationId);
        mpesaRequestDto.setB2CCommandID(b2CCommandId);
        mpesaRequestDto.setAmount(amount);
        mpesaRequestDto.setBusinessShortCode(businessShortcode);
        mpesaRequestDto.setPhoneNumber(phoneNumber);
        mpesaRequestDto.setRemarks(remarks);
        mpesaRequestDto.setQueueTimeOutURL(timeoutUrl);
        mpesaRequestDto.setResultURL(callbackUrl);
        mpesaRequestDto.setOccassion(referenceId);

        TransactionStatus transactionStatus = TransactionStatus.REQUEST_PENDING;
        String message, convoIdRes = null, conversationIdRes = null;
        try {
            //db payload
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionStatus(transactionStatus);
            transactionEntity.setMobileMoney(mobileMoney);
            transactionEntity.setTransactionType(TransactionType.B2C);
            transactionEntity.setDebitBusinessShortCode(businessShortcode);
            transactionEntity.setAmount(getAmount(amount));
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setPhoneNumber(phoneNumber + "");
            transactionEntity.setConversationId(conversationId);
            transactionEntity.setCallbackUrl(callbackUrl);
            transactionEntity.setTimeoutUrl(timeoutUrl);
            transactionEntity.setDescription(String.valueOf(b2CCommandId));
            transactionService.saveTransaction(transactionEntity);

            MpesaResponse mpesaResponse = initializeMpesaClientWithCredentials()
                    .mpesaRequestDto(mpesaRequestDto)
                    .B2CDisbursement();
            if (mpesaResponse.isInternalStatus()) {
                transactionStatus = TransactionStatus.REQUEST_ACCEPTED;
                message = mpesaResponse.getResponseDescription();
                conversationIdRes = mpesaResponse.getOriginatorConversationId();
                convoIdRes = mpesaResponse.getConversationId();
            } else {
                transactionStatus = TransactionStatus.REQUEST_FAILED;
                message = mpesaResponse.getErrorMessage();
            }
        } catch (Exception e) {
            throw new InternalErrorException(ErrorCode.ERROR, e.getMessage());
        }

        //update
        return updateTransactionRecord(referenceId, transactionStatus, message, convoIdRes, conversationIdRes);
    }

    /**
     * Remit tax to KRA
     *
     * @param mobileMoney
     * @param mobileMoneyShortcodeType
     * @param amount
     * @param remarks
     * @return TransactionDto
     */
    @Override
    public TransactionDto remitTax(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, String remarks) {
        if (mobileMoney.equals(MobileMoney.AIRTEL)) return null;

        int businessShortcode = getBusinessShortcode(mobileMoneyShortcodeType);
        String referenceId = generateReferenceId();
        String callbackUrl = Helpers.buildUrl(null, baseUrl, relativeCallbackUrl).toString();
        String timeoutUrl = Helpers.buildUrl(null, baseUrl, relativeTimeoutUrl).toString();

        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setTaxPRN(kraTaxPrn);
        mpesaRequestDto.setBusinessShortCode(businessShortcode);
        mpesaRequestDto.setAmount(amount);
        mpesaRequestDto.setRemarks(remarks);
        mpesaRequestDto.setQueueTimeOutURL(timeoutUrl);
        mpesaRequestDto.setResultURL(callbackUrl);

        TransactionStatus transactionStatus = TransactionStatus.REQUEST_PENDING;
        String message = null, convoId = null, conversationId = null;
        try {
            //db payload
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionStatus(transactionStatus);
            transactionEntity.setMobileMoney(mobileMoney);
            transactionEntity.setTransactionType(TransactionType.TAX_REMITTANCE);
            transactionEntity.setDebitBusinessShortCode(businessShortcode);
            transactionEntity.setAmount(getAmount(amount));
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setCallbackUrl(callbackUrl);
            transactionEntity.setTimeoutUrl(timeoutUrl);
            transactionEntity.setDescription(remarks);
            transactionEntity.setTaxPrn(kraTaxPrn);
            transactionService.saveTransaction(transactionEntity);

            MpesaResponse mpesaResponse = initializeMpesaClientWithCredentials()
                    .mpesaRequestDto(mpesaRequestDto)
                    .remitTax();

            if (mpesaResponse.isInternalStatus()) {
                transactionStatus = TransactionStatus.REQUEST_ACCEPTED;
                conversationId = mpesaResponse.getOriginatorConversationId();
                convoId = mpesaResponse.getConversationId();
            } else {
                transactionStatus = TransactionStatus.REQUEST_FAILED;
                message = mpesaResponse.getErrorMessage();
            }
        } catch (Exception e) {
            throw new InternalErrorException(ErrorCode.ERROR, e.getMessage());
        }

        //update
        return updateTransactionRecord(referenceId, transactionStatus, message, convoId, conversationId);
    }

    /**
     * Generate mobile money QR Code
     * @param mobileMoney
     * @param trxCodeType
     * @param amount
     * @return TransactionDto
     */
    @Override
    public TransactionDto generateQrCode(MobileMoney mobileMoney, TrxCodeType trxCodeType, double amount) {
        if (mobileMoney.equals(MobileMoney.AIRTEL)) return null;

        MobileMoneyShortcodeType mobileMoneyShortcodeType = null;
        boolean useShortcode = false;

        if (trxCodeType.equals(TrxCodeType.BUY_GOODS)) {
            mobileMoneyShortcodeType = MobileMoneyShortcodeType.BUY_GOODS;
            useShortcode = true;
        } else if (trxCodeType.equals(TrxCodeType.PAY_BILL)) {
            mobileMoneyShortcodeType = MobileMoneyShortcodeType.PAY_BILL;
            useShortcode = true;
        } else if (trxCodeType.equals(TrxCodeType.WITHDRAW_CASH_AGENT_TILL)) {
            mobileMoneyShortcodeType = MobileMoneyShortcodeType.AGENT_TILL;
        }

        String referenceId = generateReferenceId();
        int businessShortcode = mobileMoneyShortcodeType != null ? getBusinessShortcode(mobileMoneyShortcodeType) : 0;
        String businessShortcodeName = mobileMoneyShortcodeType != null ? getBusinessShortcodeName(mobileMoneyShortcodeType) : null;

        MpesaRequestDto mpesaRequestDto = new MpesaRequestDto();
        mpesaRequestDto.setAmount(amount);
        mpesaRequestDto.setMerchantName(businessShortcodeName != null ? businessShortcodeName : "mer");
        mpesaRequestDto.setRefNo(referenceId);
        mpesaRequestDto.setTrxCodeType(trxCodeType);

        if (useShortcode) {
            //if trxCodeType not among SEND_MONEY_MOBILE_NUMBER,WITHDRAW_CASH_AGENT_TILL then setBusinessShortCode
            mpesaRequestDto.setBusinessShortCode(businessShortcode);
        } else if (trxCodeType.equals(TrxCodeType.WITHDRAW_CASH_AGENT_TILL)) {
            //if trxCodeType IS WITHDRAW_CASH_AGENT_TILL then setAgentTill
            mpesaRequestDto.setAgentTill(businessShortcode);
        } else if (trxCodeType.equals(TrxCodeType.SEND_MONEY_MOBILE_NUMBER)) {
            mpesaRequestDto.setPhoneNumber(Long.valueOf(mpesaPhoneNumber));
        }

        TransactionStatus transactionStatus = TransactionStatus.REQUEST_PENDING;
        String message = null, qrCode = null;
        try {
            //db payload
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setTransactionStatus(transactionStatus);
            transactionEntity.setMobileMoney(mobileMoney);
            transactionEntity.setTransactionType(TransactionType.DYNAMIC_QR);
            transactionEntity.setAmount(getAmount(amount));
            transactionEntity.setTransactionRef(referenceId);
            transactionEntity.setTrxCodeType(trxCodeType);
            if (useShortcode || trxCodeType.equals(TrxCodeType.WITHDRAW_CASH_AGENT_TILL)) {
                transactionEntity.setCreditBusinessShortCode(businessShortcode);
            }
            if (trxCodeType.equals(TrxCodeType.SEND_MONEY_MOBILE_NUMBER)) {
                transactionEntity.setPhoneNumber(mpesaPhoneNumber);
            }
            transactionService.saveTransaction(transactionEntity);

            MpesaResponse mpesaResponse = initializeMpesaClient()
                    .mpesaRequestDto(mpesaRequestDto)
                    .generateDynamicQrCode();
            if (mpesaResponse.isInternalStatus()) {
                transactionStatus = TransactionStatus.REQUEST_ACCEPTED;
                qrCode = mpesaResponse.getQrCode();
            } else {
                transactionStatus = TransactionStatus.REQUEST_FAILED;
                message = mpesaResponse.getErrorMessage();
            }
        } catch (Exception e) {
            throw new InternalErrorException(ErrorCode.ERROR, e.getMessage());
        }

        TransactionEntity transactionEntityRecord = transactionService.getTransaction(referenceId);
        transactionEntityRecord.setTransactionStatus(transactionStatus);
        if (message != null) {
            transactionEntityRecord.setMessage(message);
        }
        if (qrCode != null) {
            transactionEntityRecord.setDescription(qrCode);
        }
        return transactionService.updateTransaction(transactionEntityRecord);

    }
}
