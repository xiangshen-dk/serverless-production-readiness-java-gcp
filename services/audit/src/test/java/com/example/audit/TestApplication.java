package com.example.audit;

import org.springframework.boot.SpringApplication;

public class TestApplication {
  public static void main(String[] args) {
    SpringApplication
        .from(AuditApplication::main)
        .with(TestcontainersConfig.class)
        .run(args);
  }
}