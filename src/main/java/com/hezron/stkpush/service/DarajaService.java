package com.hezron.stkpush.service;

import com.hezron.stkpush.config.DarajaConfig;
import com.hezron.stkpush.dto.MpesaCallback;
import com.hezron.stkpush.dto.StkPushRequest;
import com.hezron.stkpush.dto.StkPushResponse;
import com.hezron.stkpush.entity.MpesaTransaction;
import com.hezron.stkpush.repository.MpesaTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DarajaService {

    private final DarajaConfig darajaConfig;
    private final MpesaTransactionsRepository transactionsRepository;
    private final RestTemplate restTemplate;

    // STEP 1: Get Access Token
    public String getAccessToken() {

        String credentials = darajaConfig.getConsumerKey() + ":" + darajaConfig.getConsumerSecret();
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                darajaConfig.getAuthUrl(),
                HttpMethod.GET,
                request,
                Map.class
        );

        Map body = response.getBody();

        if (body == null || !body.containsKey("access_token")) {
            throw new RuntimeException("Failed to fetch access token");
        }

        String token = (String) body.get("access_token");

        log.info("Access token fetched successfully");

        return token;
    }

    // STEP 2: Initiate STK Push
    public StkPushResponse initiateStkPush(StkPushRequest request) {

        String token = getAccessToken();

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String password = Base64.getEncoder().encodeToString(
                (darajaConfig.getShortcode()
                        + darajaConfig.getPassKey()
                        + timestamp)
                        .getBytes(StandardCharsets.UTF_8)
        );

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", darajaConfig.getShortcode());
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", "CustomerPayBillOnline");
        body.put("Amount", request.getAmount().intValue());
        body.put("PartyA", formatPhone(request.getPhoneNumber()));
        body.put("PartyB", darajaConfig.getShortcode());
        body.put("PhoneNumber", formatPhone(request.getPhoneNumber()));
        body.put("CallBackURL", darajaConfig.getCallbackUrl());
        body.put("AccountReference", "HezronApp");
        body.put("TransactionDesc", "Payment");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<StkPushResponse> response = restTemplate.exchange(
                darajaConfig.getStkPushUrl(),
                HttpMethod.POST,
                entity,
                StkPushResponse.class
        );

        StkPushResponse stkResponse = response.getBody();

        if (stkResponse == null) {
            throw new RuntimeException("STK Push response was null");
        }

        MpesaTransaction transaction = new MpesaTransaction();
        transaction.setPhoneNumber(formatPhone(request.getPhoneNumber()));
        transaction.setAmount(request.getAmount());
        transaction.setCheckoutRequestID(stkResponse.getCheckoutRequestID());
        transaction.setMerchantRequestID(stkResponse.getMerchantRequestID());
        
        transaction.setStatus("PENDING");

        transactionsRepository.save(transaction);

        log.info("STK Push sent to {}", request.getPhoneNumber());

        return stkResponse;
    }

    // STEP 3: Handle Callback from Safaricom
    public void handleCallback(MpesaCallback callback) {

        var stkCallback = callback.getBody().getStkCallback();

        String checkoutRequestID = stkCallback.getCheckoutRequestID();
        String resultCode = stkCallback.getResultCode();
        String resultDesc = stkCallback.getResultDesc();

        transactionsRepository
                .findByCheckoutRequestID(checkoutRequestID)
                .ifPresent(transaction -> {

                    transaction.setResultCode(resultCode);
                    transaction.setResultDesc(resultDesc);

                    if ("0".equals(resultCode)) {
                        transaction.setStatus("SUCCESS");
                    } else {
                        transaction.setStatus("FAILED");
                    }

                    transactionsRepository.save(transaction);
                    log.info("CheckoutRequestID from callback: {}", checkoutRequestID);
                    log.info("Transaction {} updated to {}", checkoutRequestID, transaction.getStatus());
                });
    }

    // Utility: Format phone to 254XXXXXXXXX
    private String formatPhone(String phone) {

        if (phone.startsWith("0")) {
            return "254" + phone.substring(1);
        }

        if (phone.startsWith("+254")) {
            return phone.substring(1);
        }

        return phone;
    }
}