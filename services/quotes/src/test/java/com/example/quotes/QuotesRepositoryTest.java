package com.example.quotes;

import com.example.quotes.domain.QuoteRepository;
import com.example.quotes.domain.QuoteService;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuotesRepositoryTest {
  @Container
  @ServiceConnection
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine");

  @Autowired
  private QuoteRepository quoteRepository;
  private QuoteService quoteService;

  @BeforeEach
  void setUp() throws IOException {
    // print a list of all the containers test containers are currently running
    // System.out.println("Container is running: " + postgres.isRunning());
    // System.out.println("Container is healthy: " + postgres.isHealthy());
    quoteService = new QuoteService(quoteRepository);
  }

  @Test
  @DisplayName("A random quote is returned")
  void testRandomQuotes() {
    // var quote = this.quoteRepository.findRandomQuote();
    var quote = this.quoteService.findRandomQuote();
    assertThat(quote).isNotNull();
  }

}
