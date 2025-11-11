package com.shieldai.dashboard.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @PostMapping
    public String receiveAlert(@RequestBody String alertData) {
        // Logic to handle the alert
        System.out.println("Received alert: " + alertData);
        return "Alert received: ";
    }
    
   
}
