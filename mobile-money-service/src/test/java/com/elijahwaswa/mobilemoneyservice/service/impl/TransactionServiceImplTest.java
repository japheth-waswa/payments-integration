package com.elijahwaswa.mobilemoneyservice.service.impl;

import com.elijahwaswa.basedomains.enums.ConditionOperator;
import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.TransactionStatus;
import com.elijahwaswa.basedomains.enums.TransactionType;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.entity.TransactionEntity;
import com.elijahwaswa.mobilemoneyservice.exception.ResourceNotFoundException;
import com.elijahwaswa.mobilemoneyservice.repository.TransactionRepository;
import com.elijahwaswa.mobilemoneyservice.service.TransactionService;
import mpesa.util.TrxCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionServiceImplTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;

    private TransactionEntity transactionEntity1, transactionEntity2, transactionEntity3, transactionEntity4;

    private String generateRandomStr() {
        String str = UUID.randomUUID().toString();
        return str.substring(0, 8);
    }

    private void truncateH2DB() {
        //truncate or delete data from all tables
        String[] tables = {"transactions"};
        for (String table : tables) {
            String sql = "TRUNCATE TABLE " + table;
            jdbcTemplate.execute(sql);
        }
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
    void getByTransactionRef() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity4);
        TransactionEntity transactionEntity1st = transactionRepository.findByTransactionRef(transactionEntity3.getTransactionRef());
        System.out.println(transactionEntity1st);
        assertEquals(transactionEntity3.getTransactionRef(), transactionEntity1st.getTransactionRef());

        TransactionEntity transactionEntity2nd = transactionRepository.findByTransactionRef(transactionEntity1.getTransactionRef());
        System.out.println(transactionEntity2nd);
        assertEquals(transactionEntity1.getTransactionRef(), transactionEntity2nd.getTransactionRef());
    }

    @Test
    void saveTransaction() {
        TransactionDto savedTransaction = transactionService.saveTransaction(transactionEntity1);
        System.out.println(savedTransaction);
        assertEquals(transactionEntity1.getTransactionRef(), savedTransaction.getTransactionRef());
    }

    @Test
    void updateTransaction() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity4);

        System.out.println("^".repeat(50));
        System.out.println(transactionEntity3);

        //get the transaction entity
        TransactionEntity transactionEntity = transactionService.getTransaction(transactionEntity3.getTransactionRef());
        transactionEntity.setName("peter");
        TransactionDto transactionDto = transactionService.updateTransaction(transactionEntity);
        System.out.println(transactionDto);

        TransactionEntity transactionEntity1st = transactionRepository.findByTransactionRef(transactionEntity3.getTransactionRef());
        System.out.println(transactionEntity1st);
    }

    @Test
    void getTransactions_single() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        List<TransactionDto> transactions = transactionService.getTransactions(1, 1);
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity1.getTransactionRef(), transactions.get(0).getTransactionRef());
    }

    @Test
    void getTransactions_multiple() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3);
        assertEquals(3, transactions.size());
        assertEquals(transactionEntity3.getTransactionRef(), transactions.get(2).getTransactionRef());
    }

    @Test
    void getTransactions_throws_error() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(5, 3));
    }

    @Test
    void getTransactions_by_amount() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, transactionEntity3.getAmount());
        assertEquals(2, transactions.size());
        assertEquals(transactionEntity1.getTransactionRef(), transactions.get(0).getTransactionRef());
    }

    @Test
    void getTransactions_by_amount_throws_error() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(0, 3, transactionEntity3.getAmount().add(new BigDecimal(5500))));
    }

    @Test
    void getTransactions_by_amount_and_conditional_throws_error() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(0, 3, ConditionOperator.LESS_THAN, transactionEntity3.getAmount()));
    }

    @Test
    void getTransactions_by_amount_and_conditional_less_than() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, ConditionOperator.LESS_THAN, transactionEntity2.getAmount());
        assertEquals(2, transactions.size());
    }

    @Test
    void getTransactions_by_amount_and_conditional_less_than_or_equal() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, ConditionOperator.LESS_THAN_OR_EQUAL_TO, transactionEntity2.getAmount());
        assertEquals(3, transactions.size());
        assertEquals(transactionEntity1.getTransactionRef(), transactions.get(1).getTransactionRef());
    }

    @Test
    void getTransactions_by_amount_and_conditional_greater_than() {
        transactionService.saveTransaction(transactionEntity2);
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, ConditionOperator.GREATER_THAN, transactionEntity2.getAmount());
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity4.getTransactionRef(), transactions.get(0).getTransactionRef());
    }

    @Test
    void getTransactions_by_amount_and_conditional_greater_than_or_equal() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity2);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, ConditionOperator.GREATER_THAN_OR_EQUAL_TO, transactionEntity2.getAmount());
        assertEquals(2, transactions.size());
        assertEquals(transactionEntity2.getTransactionRef(), transactions.get(1).getTransactionRef());
    }

    @Test
    void getTransactions_by_transaction_status_pending() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity2);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, TransactionStatus.REQUEST_PENDING);
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity2.getTransactionRef(), transactions.get(0).getTransactionRef());
    }

    @Test
    void getTransactions_by_transaction_status_success() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, TransactionStatus.SUCCESS);
        assertEquals(2, transactions.size());
        assertEquals(transactionEntity3.getTransactionRef(), transactions.get(0).getTransactionRef());
    }

    @Test
    void getTransactions_by_transaction_status_throws() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(0, 3, TransactionStatus.REQUEST_ACCEPTED));
    }

    @Test
    void getTransactions_by_transaction_type_throws() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(0, 3, TransactionType.DYNAMIC_QR));
    }

    @Test
    void getTransactions_by_transaction_type_success() {
        transactionService.saveTransaction(transactionEntity4);
//        transactionService.saveTransaction(transactionEntity3);
//        transactionService.saveTransaction(transactionEntity1);
//        transactionService.saveTransaction(transactionEntity2);
//        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, TransactionType.B2B_BUY_GOODS);
//        assertEquals(2, transactions.size());
//        assertEquals(transactionEntity1.getTransactionRef(), transactions.get(1).getTransactionRef());
    }

    @Test
    void getTransactions_by_transaction_ref_throws() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(0, 3, "xyz"));
    }

    @Test
    void getTransactions_by_transaction_ref_success() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, transactionEntity3.getTransactionRef());
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity3.getTransactionRef(), transactions.get(0).getTransactionRef());
    }

    @Test
    void getTransactions_by_transaction_ref_mobile_money_throws() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(0, 3, "abc", MobileMoney.MPESA));
    }

    @Test
    void getTransactions_by_transaction_ref_mobile_money_success() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, transactionEntity3.getTransactionRef(), transactionEntity3.getMobileMoney());
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity3.getTransactionRef(), transactions.get(0).getTransactionRef());
    }

    @Test
    void getTransactions_by_transaction_ref_mobile_money_ref_throws() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(0, 3, transactionEntity3.getMobileMoney(), transactionEntity1.getTransactionRef()));
    }

    @Test
    void getTransactions_by_transaction_ref_mobile_money_ref_success() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity1);
        transactionService.saveTransaction(transactionEntity2);
        List<TransactionDto> transactions = transactionService.getTransactions(0, 3, transactionEntity1.getMobileMoney(), transactionEntity1.getMobileMoneyRef());
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity1.getMobileMoneyRef(), transactions.get(0).getMobileMoneyRef());
    }


    @Test
    void getTransaction() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity3);
        transactionService.saveTransaction(transactionEntity2);
        TransactionEntity transaction = transactionService.getTransaction(transactionEntity3.getTransactionRef());
        assertNotNull(transaction);
        assertEquals(transactionEntity3.getTransactionRef(),transaction.getTransactionRef());
    }

    @Test
    void testGetTransaction() {
        transactionService.saveTransaction(transactionEntity4);
        transactionService.saveTransaction(transactionEntity2);
        TransactionEntity transaction = transactionService.getTransaction(transactionEntity2.getMerchantRequestId(),transactionEntity2.getCheckoutRequestId());
        assertNotNull(transaction);
        assertEquals(transactionEntity2.getMerchantRequestId(),transaction.getMerchantRequestId());
        assertEquals(transactionEntity2.getCheckoutRequestId(),transaction.getCheckoutRequestId());
    }
}