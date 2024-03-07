package com.elijahwaswa.mobilemoneyservice.service.impl;

import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.MobileMoneyShortcodeType;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import com.elijahwaswa.mobilemoneyservice.service.MoneyService;
import mpesa.MpesaResponse;
import mpesa.dto.MpesaRequestDto;
import mpesa.util.ResponseParserType;
import mpesa.util.STKTransactionType;
import mpesa.util.TrxCodeType;

//todo for each validation, update TransactionDto in db and return that instance.
public class MoneyServiceImpl implements MoneyService {
    @Override
    public TransactionDto validateResponse(MpesaResponse mpesaResponse, ResponseParserType responseParserType) {
        return null;
    }

    @Override
    public TransactionDto initStkPush(MobileMoney mobileMoney,MobileMoneyShortcodeType mobileMoneyShortcodeType, STKTransactionType stkTransactionType, double amount, long phoneNumber, String briefDescription) {
        TransactionDto transactionDto;
        if (mobileMoney.equals(MobileMoney.AIRTEL)) {
            return null;
        }

        MpesaRequestDto mpesaRequest  = new MpesaRequestDto();
        mpesaRequest.setStkTransactionType(stkTransactionType);
        mpesaRequest.setBusinessShortCode();
        mobileMoneyShortcodeType.equals(MobileMoneyShortcodeType.BUY_GOODS)?

    }

    @Override
    public TransactionDto checkStkStatus(MobileMoney mobileMoney,MobileMoneyShortcodeType mobileMoneyShortcodeType, STKTransactionType stkTransactionType, String checkoutRequestId) {
        return null;
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
