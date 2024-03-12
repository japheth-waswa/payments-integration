package com.elijahwaswa.mobilemoneyservice.service.impl;

import base.Helpers;
import com.elijahwaswa.basedomains.enums.*;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;
import com.elijahwaswa.mobilemoneyservice.service.MoneyService;
import com.elijahwaswa.mobilemoneyservice.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import mpesa.MpesaResponse;
import mpesa.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MoneyServiceImplTest {
    @Value("${mpesa.phone_number_generic}")
    private String phoneNumberGeneric;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MoneyService moneyService;
    @Autowired
    private TransactionService transactionService;

    private TransactionEntity transactionEntity1, transactionEntity2, transactionEntity3, transactionEntity4;

    private void truncateH2DB() {
        //truncate or delete data from all tables
        String[] tables = {"transactions"};
        for (String table : tables) {
            String sql = "TRUNCATE TABLE " + table;
            jdbcTemplate.execute(sql);
        }
    }

    private String generateRandomStr() {
        String str = UUID.randomUUID().toString();
        return str.substring(0, 8);
    }

    @BeforeEach
    void setup() {
        //truncate
        truncateH2DB();

        transactionEntity1 = new TransactionEntity();
        transactionEntity1.setMobileMoney(MobileMoney.MPESA);
        transactionEntity1.setTransactionType(TransactionType.B2B_BUY_GOODS);
        transactionEntity1.setCreditBusinessShortCode(90290);
        transactionEntity1.setPhoneNumber(String.valueOf(254704313679L));
        transactionEntity1.setAmount(new BigDecimal(459_023));
        transactionEntity1.setTransactionRef(generateRandomStr());
        transactionEntity1.setMobileMoneyRef(generateRandomStr());
        transactionEntity1.setTransactionStatus(TransactionStatus.SUCCESS);
        transactionEntity1.setResponseCode(0);
        transactionEntity1.setMerchantRequestId(UUID.randomUUID().toString());
        transactionEntity1.setCheckoutRequestId(UUID.randomUUID().toString());

        transactionEntity2 = new TransactionEntity();
        transactionEntity2.setMobileMoney(MobileMoney.AIRTEL);
        transactionEntity2.setTransactionType(TransactionType.STK_QUERY);
        transactionEntity2.setCreditBusinessShortCode(49202);
        transactionEntity2.setPhoneNumber(String.valueOf(254704313679L));
        transactionEntity2.setAmount(new BigDecimal(1_234_502));
        transactionEntity2.setTransactionRef(generateRandomStr());
        transactionEntity2.setMobileMoneyRef(generateRandomStr());
        transactionEntity2.setTransactionStatus(TransactionStatus.REQUEST_PENDING);
        transactionEntity2.setResponseCode(100);
        transactionEntity2.setCheckoutRequestId(UUID.randomUUID().toString());
        transactionEntity2.setMerchantRequestId(UUID.randomUUID().toString());

        transactionEntity3 = new TransactionEntity();
        transactionEntity3.setMobileMoney(MobileMoney.MPESA);
        transactionEntity3.setTransactionType(TransactionType.B2B_BUY_GOODS);
        transactionEntity3.setCreditBusinessShortCode(3093);
        transactionEntity3.setDebitBusinessShortCode(67201);
        transactionEntity3.setPhoneNumber(String.valueOf(254704313679L));
        transactionEntity3.setAmount(new BigDecimal(459_023));
        transactionEntity3.setTransactionRef(generateRandomStr());
        transactionEntity3.setMobileMoneyRef(generateRandomStr());
        transactionEntity3.setResponseCode(340);
        transactionEntity3.setCheckoutRequestId(UUID.randomUUID().toString());
        transactionEntity3.setMerchantRequestId(UUID.randomUUID().toString());
        transactionEntity3.setTransactionStatus(TransactionStatus.SUCCESS);
        transactionEntity3.setTrxCodeType(TrxCodeType.BUY_GOODS);


        transactionEntity4 = new TransactionEntity();
        transactionEntity4.setMobileMoney(MobileMoney.AIRTEL);
        transactionEntity4.setTransactionType(TransactionType.B2B_PAY_BILL);
        transactionEntity4.setCreditBusinessShortCode(49202);
        transactionEntity4.setPhoneNumber(String.valueOf(254704313679L));
        transactionEntity4.setAmount(new BigDecimal(1_234_503));
        transactionEntity4.setTransactionRef(generateRandomStr());
        transactionEntity4.setMobileMoneyRef(generateRandomStr());
        transactionEntity4.setTransactionStatus(TransactionStatus.FAILED);
        transactionEntity4.setResponseCode(100);
        transactionEntity4.setCheckoutRequestId(UUID.randomUUID().toString());
        transactionEntity4.setMerchantRequestId(UUID.randomUUID().toString());
    }

//    @Test
//    void initStkPush() {
//        //todo test this independently from checkStkStatus
//        TransactionDto transactionDto = moneyService.initStkPush(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, STKTransactionType.PAY_BILL, 20, Long.parseLong(phoneNumberGeneric), "testing");
//        System.out.println(transactionDto);
//        //fetch the transaction
//        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
//        System.out.println(transactionEntity);
//
//        assertEquals(transactionDto.getId(), transactionEntity.getId());
//        assertEquals(transactionDto.getTransactionStatus(), transactionEntity.getTransactionStatus());
//        assertNotNull(transactionEntity.getCheckoutRequestId());
//    }

    @Test
    void checkStkStatus() {
        //todo test this independently from initStkPush
        TransactionDto transactionDto = moneyService.initStkPush(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, STKTransactionType.PAY_BILL, 20, Long.parseLong(phoneNumberGeneric), "testing");
        System.out.println(transactionDto);
        //CHECK THE STATUS
        TransactionDto transactionDto1 = moneyService.checkStkStatus(MobileMoney.MPESA, transactionDto.getTransactionRef());
        System.out.println(transactionDto1);

        assertEquals(TransactionStatus.REQUEST_ACCEPTED, transactionDto1.getTransactionStatus());
    }

    @Test
    void validateResponse_c2b_stk_success() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        String mobileMoneyRef = "NLJ7RT61SV";
        String mpesaResponseJson = String
                .format("{\"Body\":{\"stkCallback\":{\"MerchantRequestID\":\"%s\",\"CheckoutRequestID\":\"%s\",\"ResultCode\":0,\"ResultDesc\":\"The service request is processed successfully.\",\"CallbackMetadata\":{\"Item\":[{\"Name\":\"Amount\",\"Value\":1},{\"Name\":\"MpesaReceiptNumber\",\"Value\":\"%s\"},{\"Name\":\"TransactionDate\",\"Value\":20191219102115},{\"Name\":\"PhoneNumber\",\"Value\":254708374149}]}}}}",
                        transactionEntity3.getMerchantRequestId(),
                        transactionEntity3.getCheckoutRequestId(),
                        mobileMoneyRef);
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto = moneyService.validateResponse(mpesaResponse, ResponseParserType.C2B_STK);
        System.out.println(transactionDto);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionEntity3.getMerchantRequestId(), transactionEntity3.getCheckoutRequestId());
        System.out.println(transactionEntity);

        assertEquals(mobileMoneyRef, transactionEntity.getMobileMoneyRef());
        assertEquals(TransactionStatus.SUCCESS, transactionEntity.getTransactionStatus());
    }

    @Test
    void validateResponse_c2b_stk_failed() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);

        String mpesaResponseJson = String
                .format("{\"Body\":{\"stkCallback\":{\"MerchantRequestID\":\"%s\",\"CheckoutRequestID\":\"%s\",\"ResultCode\":1037,\"ResultDesc\":\"DS timeout user cannot be reached\"}}}",
                        transactionEntity1.getMerchantRequestId(),
                        transactionEntity1.getCheckoutRequestId());

        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto = moneyService.validateResponse(mpesaResponse, ResponseParserType.C2B_STK);
        System.out.println("^".repeat(50));
        System.out.println(transactionDto);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionEntity1.getMerchantRequestId(), transactionEntity1.getCheckoutRequestId());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.FAILED, transactionEntity.getTransactionStatus());
    }

    @Test
    void registerUrls() {
        TransactionDto transactionDto = moneyService.registerUrls(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, RegisterURLResponseType.COMPLETED);
        System.out.println(transactionDto);
        assertEquals(TransactionStatus.FAILED, transactionDto.getTransactionStatus());
    }

    @Test
    void testDoubleAndBigDecimal() {
        BigDecimal x = new BigDecimal("89.05");
        double y = 89.054;
        boolean z = x.equals(new BigDecimal(String.valueOf(y)));
        System.out.println(x);
        System.out.println(y);
        System.out.println(new BigDecimal(String.valueOf(y)));
        System.out.println(z);
//        System.out.println(x.compareTo(new BigDecimal(y)));
//        System.out.println(x.compareTo(new BigDecimal(String.valueOf(y))));
    }

    @Test
    void testRoundingBigDecimal() {
        BigDecimal x = new BigDecimal("89.05");
        double y = 89.054;
        double w = 89.055;
        double t = 89.05;
        double er = 89;
        System.out.println(x.setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE));
        System.out.println(new BigDecimal(y).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE));
        System.out.println(new BigDecimal(w).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE));
        System.out.println(new BigDecimal(t).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE));
        System.out.println(new BigDecimal(er).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE));
        System.out.println(new BigDecimal(y).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE).equals(new BigDecimal(t).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE)));
        System.out.println(x.equals(new BigDecimal(y).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE)));
        System.out.println(x.equals(new BigDecimal(t).setScale(MoneyServiceImpl.ROUNDING_DECIMALS, MoneyServiceImpl.ROUNDING_MODE)));
    }

    @Test
    void parseMobileMoneyResponse_pay_bill_failed() throws JsonProcessingException {
        String transactionType = "Pay Bill";
        String billRefNumber = "NLJ7RT61SV";
        String transactionRef = "9SM20D9J1D";
        String mpesaResponseJson = String
                .format("{\"TransactionType\":\"%s\",\"TransID\":\"%s\",\"TransTime\":\"20191122063845\",\"TransAmount\":\"10\",\"BusinessShortCode\":\"600638\",\"BillRefNumber\":\"%s\",\"InvoiceNumber\":\"\",\"OrgAccountBalance\":\"\",\"ThirdPartyTransID\":\"\",\"MSISDN\":\"25470****149\",\"FirstName\":\"John\",\"MiddleName\":\"\",\"LastName\":\"Doe\"}",
                        transactionType,
                        transactionRef,
                        billRefNumber);

        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);
        MpesaResponse mpesaResponse1 = moneyService.parseMobileMoneyResponse(MobileMoneyResponseType.VALIDATION, mpesaResponse);
        System.out.println(mpesaResponse1);
        assertNotNull(mpesaResponse1.getResultCode());
        assertNotNull(mpesaResponse1.getResultDesc());
    }

    @Test
    void parseMobileMoneyResponse_pay_bill_validation_success() throws JsonProcessingException {
        String transactionType = "Pay Bill";
        String transactionRef = "9SM20D9J1D";
        String phoneNumber = "254704598149";
        double amount = 10;

        transactionEntity4.setTransactionRef(transactionRef);
        transactionEntity4.setAmount(new BigDecimal(amount));
        transactionEntity4.setPhoneNumber(phoneNumber);

        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity4);


        String mpesaResponseJson = String
                .format("{\"TransactionType\":\"%s\",\"TransID\":\"NLJ7RT61SV\",\"TransTime\":\"20191122063845\",\"TransAmount\":\"%s\",\"BusinessShortCode\":\"600638\",\"BillRefNumber\":\"%s\",\"InvoiceNumber\":\"\",\"OrgAccountBalance\":\"\",\"ThirdPartyTransID\":\"\",\"MSISDN\":\"%s\",\"FirstName\":\"John\",\"MiddleName\":\"\",\"LastName\":\"Doe\"}",
                        transactionType,
                        amount,
                        transactionRef,
                        phoneNumber.substring(0, 5) + "****" + phoneNumber.substring(9)
                );

        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);
        MpesaResponse mpesaResponse1 = moneyService.parseMobileMoneyResponse(MobileMoneyResponseType.VALIDATION, mpesaResponse);
        System.out.println(mpesaResponse1);
        assertEquals("0", mpesaResponse1.getResultCode());
    }

    @Test
    void parseMobileMoneyResponse_pay_bill_confirmation_exists_success() throws JsonProcessingException {
        String transactionType = "Pay Bill";
        String transactionRef = "9SM20D9J1D";
        String phoneNumber = "254704598149";
        double amount = 10;

        transactionEntity4.setTransactionRef(transactionRef);
        transactionEntity4.setAmount(new BigDecimal(amount));
        transactionEntity4.setPhoneNumber(phoneNumber);

        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity4);


        String mpesaResponseJson = String
                .format("{\"TransactionType\":\"%s\",\"TransID\":\"NLJ7RT61SV\",\"TransTime\":\"20191122063845\",\"TransAmount\":\"%s\",\"BusinessShortCode\":\"500789\",\"BillRefNumber\":\"%s\",\"InvoiceNumber\":\"\",\"OrgAccountBalance\":\"\",\"ThirdPartyTransID\":\"\",\"MSISDN\":\"%s\",\"FirstName\":\"John\",\"MiddleName\":\"\",\"LastName\":\"Doe\"}",
                        transactionType,
                        amount,
                        transactionRef,
                        phoneNumber.substring(0, 5) + "****" + phoneNumber.substring(9)
                );

        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);
        MpesaResponse mpesaResponse1 = moneyService.parseMobileMoneyResponse(MobileMoneyResponseType.CONFIRMATION, mpesaResponse);
        System.out.println(mpesaResponse1);
        assertEquals("0", mpesaResponse1.getResultCode());
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionRef);
        System.out.println(transactionEntity);
        assertEquals(transactionEntity4.getCreditBusinessShortCode(), transactionEntity.getCreditBusinessShortCode());
    }

    @Test
    void parseMobileMoneyResponse_pay_bill_confirmation_does_not_exists_success() throws JsonProcessingException {
        String transactionType = "Pay Bill";
        String transactionRef = "9SM20D9J1D";
        String phoneNumber = "254704598149";
        double amount = 10;


        String mpesaResponseJson = String
                .format("{\"TransactionType\":\"%s\",\"TransID\":\"NLJ7RT61SV\",\"TransTime\":\"20191122063845\",\"TransAmount\":\"%s\",\"BusinessShortCode\":\"500789\",\"BillRefNumber\":\"%s\",\"InvoiceNumber\":\"\",\"OrgAccountBalance\":\"\",\"ThirdPartyTransID\":\"\",\"MSISDN\":\"%s\",\"FirstName\":\"John\",\"MiddleName\":\"\",\"LastName\":\"Doe\"}",
                        transactionType,
                        amount,
                        transactionRef,
                        phoneNumber.substring(0, 5) + "****" + phoneNumber.substring(9)
                );

        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);
        MpesaResponse mpesaResponse1 = moneyService.parseMobileMoneyResponse(MobileMoneyResponseType.CONFIRMATION, mpesaResponse);
        System.out.println(mpesaResponse1);
        assertEquals("0", mpesaResponse1.getResultCode());
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionRef);
        System.out.println(transactionEntity);
        assertEquals(500789, transactionEntity.getCreditBusinessShortCode());
    }

    @Test
    void parseMobileMoneyResponse_buy_goods_confirmation_success() throws JsonProcessingException {
        String transactionType = "Buy Goods";
        String transactionRef = "NLJ7RT61SV";
        String TransId = "9SM20D9J1D";
        String mpesaResponseJson = String
                .format("{\"TransactionType\":\"%s\",\"TransID\":\"%s\",\"TransTime\":\"20191122063845\",\"TransAmount\":\"10\",\"BusinessShortCode\":\"600638\",\"BillRefNumber\":\"%s\",\"InvoiceNumber\":\"\",\"OrgAccountBalance\":\"\",\"ThirdPartyTransID\":\"\",\"MSISDN\":\"25470****149\",\"FirstName\":\"John\",\"MiddleName\":\"\",\"LastName\":\"Doe\"}",
                        transactionType,
                        TransId,
                        transactionRef
                );

        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);
        MpesaResponse mpesaResponse1 = moneyService.parseMobileMoneyResponse(MobileMoneyResponseType.CONFIRMATION, mpesaResponse);
        System.out.println(mpesaResponse1);
        assertEquals("0", mpesaResponse1.getResultCode());
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionRef);
        System.out.println(transactionEntity);
        assertEquals(600638, transactionEntity.getCreditBusinessShortCode());
    }

    @Test
    void businessToBusinessPayment_pay_bill() {
        TransactionDto transactionDto = moneyService.businessToBusinessPayment(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, 20, 90290, 254704313679L, "testing");
        System.out.println(transactionDto);
        //fetch the transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(transactionDto.getId(), transactionEntity.getId());
        assertEquals(transactionDto.getTransactionStatus(), transactionEntity.getTransactionStatus());
    }

    @Test
    void businessToBusinessPayment_buy_goods() {
        TransactionDto transactionDto = moneyService.businessToBusinessPayment(MobileMoney.MPESA, MobileMoneyShortcodeType.BUY_GOODS, 20, 90290, 254704313679L, "NEWONE");
        System.out.println(transactionDto);
        //fetch the transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(transactionDto.getId(), transactionEntity.getId());
        assertEquals(transactionDto.getTransactionStatus(), transactionEntity.getTransactionStatus());
    }

    @Test
    void validateResponse_b2b_payment_success() throws JsonProcessingException {
        transactionEntity1.setConversationId(UUID.randomUUID().toString());
        transactionEntity1.setConvoId(UUID.randomUUID().toString());

        transactionEntity4.setConversationId(UUID.randomUUID().toString());
        transactionEntity4.setConvoId(UUID.randomUUID().toString());

        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity1);
        String mobileMoneyRef = "QKA81LK5CY";
        String mpesaResponseJson = String
                .format("{\"Result\":{\"ResultType\":\"0\",\"ResultCode\":\"0\",\"ResultDesc\":\"The service request is processed successfully\",\"OriginatorConversationID\":\"%s\",\"ConversationID\":\"%s\",\"TransactionID\":\"%s\",\"ResultParameters\":{\"ResultParameter\":[{\"Key\":\"DebitAccountBalance\",\"Value\":\"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}\"},{\"Key\":\"Amount\",\"Value\":\"190.00\"},{\"Key\":\"DebitPartyAffectedAccountBalance\",\"Value\":\"Working Account|KES|346568.83|6186.83|340382.00|0.00\"},{\"Key\":\"TransCompletedTime\",\"Value\":\"20221110110717\"},{\"Key\":\"DebitPartyCharges\",\"Value\":\"\"},{\"Key\":\"ReceiverPartyPublicName\",\"Value\":\"000000– Biller Companty\"},{\"Key\":\"Currency\",\"Value\":\"KES\"},{\"Key\":\"InitiatorAccountCurrentBalance\",\"Value\":\"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}\"}]},\"ReferenceData\":{\"ReferenceItem\":[{\"Key\":\"BillReferenceNumber\",\"Value\":\"19008\"},{\"Key\":\"QueueTimeoutURL\",\"Value\":\"https://mydomain.com/b2b/businessbuygoods/queue/\"}]}}}",
                        transactionEntity1.getConversationId(),
                        transactionEntity1.getConvoId(),
                        mobileMoneyRef);
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto = moneyService.validateResponse(mpesaResponse, ResponseParserType.B2B_PAYMENT);
        System.out.println(transactionDto);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionEntity1.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(mobileMoneyRef, transactionEntity.getMobileMoneyRef());
        assertEquals(TransactionStatus.SUCCESS, transactionEntity.getTransactionStatus());
    }

    @Test
    void validateResponse_b2b_payment_failed() throws JsonProcessingException {
        transactionEntity3.setConversationId(UUID.randomUUID().toString());
        transactionEntity3.setConvoId(UUID.randomUUID().toString());

        transactionEntity2.setConversationId(UUID.randomUUID().toString());
        transactionEntity2.setConvoId(UUID.randomUUID().toString());

        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity3);
        String mpesaResponseJson = String
                .format("{\"Result\":{\"ResultType\":0,\"ResultCode\":17,\"ResultDesc\":\"System internal error.\",\"OriginatorConversationID\":\"%s\",\"ConversationID\":\"%s\",\"TransactionID\":\"0000000000000000\",\"ReferenceData\":{\"ReferenceItem\":[{\"Key\":\"BillReferenceNumber\",\"Value\":\"1d2ea2b9\"},{\"Key\":\"QueueTimeoutURL\",\"Value\":\"https://internalsandbox.safaricom.co.ke/mpesa/b2bresults/v1/submit\"},{\"Key\":\"Occassion\"}]}}}",
                        transactionEntity2.getConversationId(),
                        transactionEntity2.getConvoId()
                );
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto = moneyService.validateResponse(mpesaResponse, ResponseParserType.B2B_PAYMENT);
        System.out.println(transactionDto);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransactionByConversationId(transactionEntity2.getConversationId(), transactionEntity2.getConvoId());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.FAILED, transactionEntity.getTransactionStatus());
    }

    @Test
    void businessToBusinessPaymentStkPush() {
        TransactionDto transactionDto = moneyService.businessToBusinessPaymentStkPush(MobileMoney.MPESA, MobileMoneyShortcodeType.BUY_GOODS, 10, 174379, "Test");
        System.out.println(transactionDto);
        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);
        assertEquals(transactionDto.getId(), transactionEntity.getId());
        assertEquals(TransactionStatus.REQUEST_ACCEPTED, transactionEntity.getTransactionStatus());
    }

    @Test
    void validateResponse_b2b_stk_success() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity4);

        TransactionDto transactionDto = moneyService.businessToBusinessPaymentStkPush(MobileMoney.MPESA, MobileMoneyShortcodeType.BUY_GOODS, 10, 174379, "Test");
        System.out.println(transactionDto);

        String mobileMoneyRef = "RDQ01NFT1Q";
        String mpesaResponseJson = String
                .format("{\"resultCode\":\"0\",\"resultDesc\":\"The service request is processed successfully.\",\"amount\":\"71.0\",\"requestId\":\"%s\",\"resultType\":\"0\",\"conversationID\":\"AG_20230426_2010434680d9f5a73766\",\"transactionId\":\"%s\",\"status\":\"SUCCESS\"}",
                        transactionDto.getRequestId(),
                        mobileMoneyRef
                );
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto1 = moneyService.validateResponse(mpesaResponse, ResponseParserType.B2B_STK);
        System.out.println(transactionDto1);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.SUCCESS, transactionEntity.getTransactionStatus());
        assertEquals(mobileMoneyRef, transactionEntity.getMobileMoneyRef());
    }
    @Test
    void validateResponse_b2b_stk_failed() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity4);

        TransactionDto transactionDto = moneyService.businessToBusinessPaymentStkPush(MobileMoney.MPESA, MobileMoneyShortcodeType.BUY_GOODS, 10, 174379, "Test");
        System.out.println(transactionDto);

        String mpesaResponseJson = String
                .format("{\"resultCode\":\"4001\",\"resultDesc\":\"User cancelled transaction\",\"requestId\":\"%s\",\"amount\":\"71.0\",\"paymentReference\":\"MAndbubry3hi\"}",
                        transactionDto.getRequestId()
                );
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto1 = moneyService.validateResponse(mpesaResponse, ResponseParserType.B2B_STK);
        System.out.println(transactionDto1);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.FAILED, transactionEntity.getTransactionStatus());
    }

    @Test
    void businessToCustomerDisbursement() {
        TransactionDto transactionDto = moneyService.businessToCustomerDisbursement(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, B2CCommandID.PROMOTION_PAYMENT, 254704313679L, 10, "Test");
        System.out.println(transactionDto);
        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);
        assertEquals(transactionDto.getId(), transactionEntity.getId());
        assertEquals(TransactionStatus.REQUEST_ACCEPTED, transactionEntity.getTransactionStatus());
    }

    @Test
    void validateResponse_b2c_success() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);

        TransactionDto transactionDto = moneyService.businessToCustomerDisbursement(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, B2CCommandID.PROMOTION_PAYMENT, 254704313679L, 10, "Test");
        System.out.println(transactionDto);

        String mobileMoneyRef = "GIP37HAY6Z";
        String mpesaResponseJson = String
                .format("{\"Result\":{\"ResultType\":0,\"ResultCode\":0,\"ResultDesc\":\"The service request is processed successfully.\",\"OriginatorConversationID\":\"%s\",\"ConversationID\":\"%s\",\"TransactionID\":\"%s\",\"ResultParameters\":{\"ResultParameter\":[{\"Key\":\"TransactionAmount\",\"Value\":10},{\"Key\":\"TransactionReceipt\",\"Value\":\"NLJ41HAY6Q\"},{\"Key\":\"B2CRecipientIsRegisteredCustomer\",\"Value\":\"Y\"},{\"Key\":\"B2CChargesPaidAccountAvailableFunds\",\"Value\":-4510},{\"Key\":\"ReceiverPartyPublicName\",\"Value\":\"254708374149 - John Doe\"},{\"Key\":\"TransactionCompletedDateTime\",\"Value\":\"19.12.2019 11:45:50\"},{\"Key\":\"B2CUtilityAccountAvailableFunds\",\"Value\":10116},{\"Key\":\"B2CWorkingAccountAvailableFunds\",\"Value\":900000}]},\"ReferenceData\":{\"ReferenceItem\":{\"Key\":\"QueueTimeoutURL\",\"Value\":\"https://internalsandbox.safaricom.co.ke/mpesa/b2cresults/v1/submit\"}}}}",
                        transactionDto.getConversationId(),
                        transactionDto.getConvoId(),
                        mobileMoneyRef
                );
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto1 = moneyService.validateResponse(mpesaResponse, ResponseParserType.B2C);
        System.out.println(transactionDto1);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.SUCCESS, transactionEntity.getTransactionStatus());
        assertEquals(mobileMoneyRef, transactionEntity.getMobileMoneyRef());
    }
    @Test
    void validateResponse_b2c_failed() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity2);

        TransactionDto transactionDto = moneyService.businessToCustomerDisbursement(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, B2CCommandID.PROMOTION_PAYMENT, 254704313679L, 10, "Test");
        System.out.println(transactionDto);

        String mobileMoneyRef = "UIP52GAY6T";
        String mpesaResponseJson = String
                .format("{\"Result\":{\"ResultType\":0,\"ResultCode\":2001,\"ResultDesc\":\"The initiator information is invalid.\",\"OriginatorConversationID\":\"%s\",\"ConversationID\":\"%s\",\"TransactionID\":\"%s\",\"ReferenceData\":{\"ReferenceItem\":{\"Key\":\"QueueTimeoutURL\",\"Value\":\"https://internalsandbox.safaricom.co.ke/mpesa/b2cresults/v1/submit\"}}}}",
                        transactionDto.getConversationId(),
                        transactionDto.getConvoId(),
                        mobileMoneyRef
                );
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto1 = moneyService.validateResponse(mpesaResponse, ResponseParserType.B2C);
        System.out.println(transactionDto1);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.FAILED, transactionEntity.getTransactionStatus());
        assertEquals(mobileMoneyRef, transactionEntity.getMobileMoneyRef());
    }

    @Test
    void remitTax() {
        TransactionDto transactionDto = moneyService.remitTax(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, 10, "Test");
        System.out.println(transactionDto);
        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);
        assertEquals(transactionDto.getId(), transactionEntity.getId());
        assertEquals(TransactionStatus.REQUEST_ACCEPTED, transactionEntity.getTransactionStatus());
    }

    @Test
    void validateResponse_remit_tax_success() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity2);

        TransactionDto transactionDto = moneyService.remitTax(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, 10, "Test");
        System.out.println(transactionDto);

        String mobileMoneyRef = "QKA81LK5CY";
        String mpesaResponseJson = String
                .format("{\"Result\":{\"ResultType\":\"0\",\"ResultCode\":\"0\",\"ResultDesc\":\"The service request is processed successfully\",\"OriginatorConversationID\":\"%s\",\"ConversationID\":\"%s\",\"TransactionID\":\"%s\",\"ResultParameters\":{\"ResultParameter\":[{\"Key\":\"DebitAccountBalance\",\"Value\":\"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}\"},{\"Key\":\"Amount\",\"Value\":\"190.00\"},{\"Key\":\"DebitPartyAffectedAccountBalance\",\"Value\":\"Working Account|KES|346568.83|6186.83|340382.00|0.00\"},{\"Key\":\"TransCompletedTime\",\"Value\":\"20221110110717\"},{\"Key\":\"DebitPartyCharges\",\"Value\":\"\"},{\"Key\":\"ReceiverPartyPublicName\",\"Value\":\"00000 - Tax Collecting Company\"},{\"Key\":\"Currency\",\"Value\":\"KES\"},{\"Key\":\"InitiatorAccountCurrentBalance\",\"Value\":\"{Amount={CurrencyCode=KES, MinimumAmount=618683, BasicAmount=6186.83}}\"}]},\"ReferenceData\":{\"ReferenceItem\":[{\"Key\":\"BillReferenceNumber\",\"Value\":\"19008\"},{\"Key\":\"QueueTimeoutURL\",\"Value\":\"https://mydomain.com/b2b/remittax/queue/\"}]}}}",
                        transactionDto.getConversationId(),
                        transactionDto.getConvoId(),
                        mobileMoneyRef
                );
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto1 = moneyService.validateResponse(mpesaResponse, ResponseParserType.TAX_REMITTANCE);
        System.out.println(transactionDto1);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.SUCCESS, transactionEntity.getTransactionStatus());
        assertEquals(mobileMoneyRef, transactionEntity.getMobileMoneyRef());
    }
    @Test
    void validateResponse_remit_tax_failed() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity2);

        TransactionDto transactionDto = moneyService.remitTax(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, 10, "Test");
        System.out.println(transactionDto);

        String mobileMoneyRef = "SBR12IFWUT";
        String mpesaResponseJson = String
                .format("{\"Result\":{\"ResultType\":0,\"ResultCode\":2001,\"ResultDesc\":\"The initiator information is invalid.\",\"OriginatorConversationID\":\"%s\",\"ConversationID\":\"%s\",\"TransactionID\":\"%s\",\"ReferenceData\":{\"ReferenceItem\":[{\"Key\":\"BillReferenceNumber\",\"Value\":353353},{\"Key\":\"QueueTimeoutURL\",\"Value\":\"https://internalsandbox.safaricom.co.ke/mpesa/b2bresults/v1/submit\"},{\"Key\":\"Occassion\"}]}}}",
                        transactionDto.getConversationId(),
                        transactionDto.getConvoId(),
                        mobileMoneyRef
                );
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class, mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto1 = moneyService.validateResponse(mpesaResponse, ResponseParserType.TAX_REMITTANCE);
        System.out.println(transactionDto1);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.FAILED, transactionEntity.getTransactionStatus());
        assertEquals(mobileMoneyRef, transactionEntity.getMobileMoneyRef());
    }

    @Test
    void generateQrCode_failed() {
        TransactionDto transactionDto = moneyService.generateQrCode(MobileMoney.MPESA, TrxCodeType.BUY_GOODS, 10);
        System.out.println(transactionDto);
        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);
        assertEquals(transactionDto.getId(), transactionEntity.getId());
        assertEquals(TransactionStatus.REQUEST_FAILED, transactionEntity.getTransactionStatus());
    }
}