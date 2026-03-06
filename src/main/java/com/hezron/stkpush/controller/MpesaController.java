package com.hezron.stkpush.controller;


import com.hezron.stkpush.dto.MpesaCallback;
import com.hezron.stkpush.dto.StkPushRequest;
import com.hezron.stkpush.dto.StkPushResponse;
import com.hezron.stkpush.service.DarajaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/mpesa")
@RequiredArgsConstructor
public class MpesaController {

    private final DarajaService darajaService;

    //trigger stk push
    @PostMapping("/stk-push")
    public ResponseEntity<StkPushResponse> stkPush(@RequestBody StkPushRequest request) {
        log.info("STK Push requested for {}", request.getPhoneNumber());
        StkPushResponse response = darajaService.initiateStkPush(request);
        return ResponseEntity.ok(response);
    }

    //safaricom callback (called by saf)
    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody MpesaCallback callback) {
        log.info("Callback received from safaricom");
        darajaService.handleCallback(callback);
        return ResponseEntity.ok("Callback received");
    }

    //health check
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("M-Pesa service is up and running!");
    }

}
