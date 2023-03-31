package com.redjanvier.limiterService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@RequestMapping("/api/v1")
public class Limiter {
  final private String BASE_URL = "http://localhost:8080/api/v1";
  final private String NOTIFICATION_METHOD = "SMS";

  @Autowired
  private RestTemplate restTemplate;

  @GetMapping
  @RateLimiter(name = "NoficaLimiter", fallbackMethod = "handleSysTooManyRequests")
  public ResponseEntity<String> notifyUser() {
    String res = restTemplate.getForObject(BASE_URL + "/notification/" + NOTIFICATION_METHOD, String.class);
    return new ResponseEntity<String>(res, HttpStatus.OK);
  }

  public void handleSysTooManyRequests() {
    
  }
}
