package com.example.quotes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
public class QuoteControllerTest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine");

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturnQuote() throws Exception {
    mockMvc.perform(get("/quotes"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        // .andExpect(jsonPath("$.size()", Matchers.is(3)));
        // .andExpect(jsonPath("$[0].code", CoreMatchers.equalTo("P10")));
        // .andExpect(jsonPath("$[0].name", CoreMatchers.equalTo("pname1")))
        // .andExpect(jsonPath("$[0].description", CoreMatchers.equalTo("pdescr1")))
        // .andExpect(jsonPath("$[0].price", CoreMatchers.equalTo(10.0)))
    ;
  }
}
