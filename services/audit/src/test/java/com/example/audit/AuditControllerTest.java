/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

// @RunWith(SpringRunner.class)
// @SpringBootTest
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@Testcontainers
@AutoConfigureMockMvc
public class AuditControllerTest {
    @Autowired
    private MockMvc mockMvc;
    String mockBody;

    @BeforeEach
    public void setup() throws JSONException {
        JSONObject message =
                new JSONObject()
                        .put("quote", "test quote")
                        .put("author", "anonymous")
                        .put("book", "new book")
                        .put("randomId", UUID.randomUUID())
                        .put("attributes", new JSONObject());
        mockBody = new JSONObject().put("message", message).toString();
    }

    @Disabled
    @Test
    public void goodTest() throws Exception {
        mockMvc
                .perform(
                        post("/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mockBody)
                                .header("ce-id", "test id")
                                .header("ce-source", "test source")
                                .header("ce-type", "test type")
                                .header("ce-specversion", "test specversion")
                                .header("ce-subject", "test subject"))
                // .andExpect(status().isOk());
            .andExpect((status().is4xxClientError()));
    }

    @Test
    public void addEmptyBody() throws Exception {
        mockMvc.perform(post("/")).andExpect(status().isBadRequest());
    }

    @Test
    public void addNoMessage() throws Exception {
        mockMvc
                .perform(post("/").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addInvalidMimetype() throws Exception {
        mockMvc
                .perform(post("/").contentType(MediaType.TEXT_HTML).content(mockBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void addRequiredHeaders() throws Exception {
        mockMvc
                .perform(
                        post("/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mockBody)
                                .header("ce-id", "test")
                                .header("ce-source", "test")
                                .header("ce-type", "test")
                                .header("ce-specversion", "test"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void missingRequiredHeaders() throws Exception {
        mockMvc
                .perform(
                        post("/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mockBody)
                                .header("ce-source", "test")
                                .header("ce-type", "test")
                                .header("ce-specversion", "test")
                                .header("ce-subject", "test"))
                .andExpect(status().isBadRequest());

        mockMvc
                .perform(
                        post("/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mockBody)
                                .header("ce-id", "test")
                                .header("ce-source", "test")
                                .header("ce-type", "test")
                                .header("ce-specversion", "test"))
                .andExpect(status().isBadRequest());
    }
}
