package com.example.quotes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class QuotesControllerTest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine");

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturnQuotes() throws Exception {
    mockMvc.perform(get("/quotes"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void shouldReturnQuoteByAuthor() throws Exception {
    mockMvc.perform(get("/quotes/author/George Orwell"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].author", Matchers.equalTo("George Orwell")));
  }

  @Test
  void shouldSaveProduct() throws Exception {
    mockMvc.perform(
            post("/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                  {
                  "author": "Isabel Allende",
                  "quote": "The longer I live, the more uninformed I feel. Only the young have an explanation for everything.",
                  "book": "City of the Beasts"
                  }
                  """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", Matchers.notNullValue()))
        .andExpect(jsonPath("$.author", Matchers.equalTo("Isabel Allende")))
        .andExpect(jsonPath("$.quote", Matchers.equalTo("The longer I live, the more uninformed I feel. Only the young have an explanation for everything.")))
        .andExpect(jsonPath("$.book", Matchers.equalTo("City of the Beasts")));
  }
}
