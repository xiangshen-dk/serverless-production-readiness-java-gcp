package com.example.quotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuotesApplication {

  public static void main(String[] args) {
    Runtime r = Runtime.getRuntime();

    System.out.println("Runtime Data:");
    System.out.println("QuotesApplication: Active processors: " + r.availableProcessors());    
		System.out.println("QuotesApplication: Total memory: " + r.totalMemory()); 
		System.out.println("QuotesApplication: Free memory: " + r.freeMemory()); 
		System.out.println("QuotesApplication: Max memory: " + r.maxMemory()); 

    SpringApplication.run(QuotesApplication.class, args);
  }
}
