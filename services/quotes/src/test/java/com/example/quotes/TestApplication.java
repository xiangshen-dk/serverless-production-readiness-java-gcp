package com.example.quotes;

import org.springframework.boot.SpringApplication;

public class TestApplication {
  public static void main(String[] args) {
    SpringApplication
        .from(QuotesApplication::main)
        .with(TestcontainersConfig.class)
        .run(args);
  }
}