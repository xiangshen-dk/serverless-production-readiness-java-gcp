package com.example.quotes.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@RestController
public class HelloController {

    @Value("${target:local}")
    String target;

    @GetMapping("/hello")
    public String hello()
    {
        return String.format("Hello from your %s environment!", target);
    }
}
