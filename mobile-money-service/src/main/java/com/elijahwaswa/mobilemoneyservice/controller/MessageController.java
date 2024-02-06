package com.elijahwaswa.mobilemoneyservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class MessageController {

    @Value("${test.properties.refresh}")
    private String message;

    @GetMapping("message")
    public String message(){
        return message;
    }

}
