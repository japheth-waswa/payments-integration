package com.elijahwaswa.mobilemoneyservice.service.impl;

import base.Helpers;
import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.MobileMoneyShortcodeType;
import com.elijahwaswa.basedomains.enums.TransactionStatus;
import com.elijahwaswa.basedomains.enums.TransactionType;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;
import com.elijahwaswa.mobilemoneyservice.service.MoneyService;
import com.elijahwaswa.mobilemoneyservice.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import mpesa.MpesaResponse;
import mpesa.util.ResponseParserType;
import mpesa.util.STKTransactionType;
import mpesa.util.TrxCodeType;
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
        transactionEntity1.setPhoneNumber(254704313679L);
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
        transactionEntity2.setPhoneNumber(254704313679L);
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
        transactionEntity3.setPhoneNumber(254704313679L);
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
        transactionEntity4.setPhoneNumber(254704313679L);
        transactionEntity4.setAmount(new BigDecimal(1_234_503));
        transactionEntity4.setTransactionRef(generateRandomStr());
        transactionEntity4.setMobileMoneyRef(generateRandomStr());
        transactionEntity4.setTransactionStatus(TransactionStatus.FAILED);
        transactionEntity4.setResponseCode(100);
        transactionEntity4.setCheckoutRequestId(UUID.randomUUID().toString());
        transactionEntity4.setMerchantRequestId(UUID.randomUUID().toString());
    }

    @Test
    void initStkPush() {
        //todo test this independently from checkStkStatus
        TransactionDto transactionDto  = moneyService.initStkPush(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, STKTransactionType.PAY_BILL,20,Long.parseLong(phoneNumberGeneric),"testing");
        System.out.println(transactionDto);
        //fetch the transaction
        TransactionEntity transactionEntity= transactionService.getTransaction(transactionDto.getTransactionRef());
        System.out.println(transactionEntity);

        assertEquals(transactionDto.getId(),transactionEntity.getId());
        assertEquals(transactionDto.getTransactionStatus(),transactionEntity.getTransactionStatus());
        assertNotNull(transactionEntity.getCheckoutRequestId());
    }

    @Test
    void checkStkStatus() {
        //todo test this independently from initStkPush
        TransactionDto transactionDto  = moneyService.initStkPush(MobileMoney.MPESA, MobileMoneyShortcodeType.PAY_BILL, STKTransactionType.PAY_BILL,20,Long.parseLong(phoneNumberGeneric),"testing");
        System.out.println(transactionDto);
        //CHECK THE STATUS
        TransactionDto transactionDto1 = moneyService.checkStkStatus(MobileMoney.MPESA, transactionDto.getTransactionRef());
        System.out.println(transactionDto1);

        assertEquals(TransactionStatus.REQUEST_ACCEPTED,transactionDto1.getTransactionStatus());
    }

    @Test
    void validateResponse_c2b_stk_success() throws JsonProcessingException {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        String mobileMoneyRef="NLJ7RT61SV";
        String mpesaResponseJson = String
                .format("{\"Body\":{\"stkCallback\":{\"MerchantRequestID\":\"%s\",\"CheckoutRequestID\":\"%s\",\"ResultCode\":0,\"ResultDesc\":\"The service request is processed successfully.\",\"CallbackMetadata\":{\"Item\":[{\"Name\":\"Amount\",\"Value\":1},{\"Name\":\"MpesaReceiptNumber\",\"Value\":\"%s\"},{\"Name\":\"TransactionDate\",\"Value\":20191219102115},{\"Name\":\"PhoneNumber\",\"Value\":254708374149}]}}}}",
                        transactionEntity3.getMerchantRequestId(),
                        transactionEntity3.getCheckoutRequestId(),
                        mobileMoneyRef);
        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class,mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto = moneyService.validateResponse(mpesaResponse, ResponseParserType.C2B_STK);
        System.out.println(transactionDto);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionEntity3.getMerchantRequestId(),transactionEntity3.getCheckoutRequestId());
        System.out.println(transactionEntity);

        assertEquals(mobileMoneyRef,transactionEntity.getMobileMoneyRef());
        assertEquals(TransactionStatus.SUCCESS,transactionEntity.getTransactionStatus());
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

        MpesaResponse mpesaResponse = Helpers.jsonToPOJO(MpesaResponse.class,mpesaResponseJson);

        //parse mpesa response
        TransactionDto transactionDto = moneyService.validateResponse(mpesaResponse, ResponseParserType.C2B_STK);
        System.out.println("^".repeat(50));
        System.out.println(transactionDto);

        //fetch transaction
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionEntity1.getMerchantRequestId(),transactionEntity1.getCheckoutRequestId());
        System.out.println(transactionEntity);

        assertEquals(TransactionStatus.FAILED,transactionEntity.getTransactionStatus());
    }
}