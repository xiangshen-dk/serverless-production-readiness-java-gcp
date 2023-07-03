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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;

import com.example.audit.actuator.StartupCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.*;
import com.google.api.core.ApiFuture;

@RestController
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private static final List<String> requiredFields = Arrays.asList("ce-id", "ce-source", "ce-type", "ce-specversion");

    @Autowired
    private EventService eventService;

    @PostConstruct
    public void init() {
        logger.info("AuditApplication: EventController Post Construct Initializer " + new SimpleDateFormat("HH:mm:ss.SSS").format(new java.util.Date(System.currentTimeMillis())));
        logger.info("AuditApplication: EventController Post Construct - StartupCheck can be enabled");

        StartupCheck.up();
    }

    @GetMapping("start")
    String start() {
        logger.info("AuditApplication: EventController - Executed start endpoint request " + new SimpleDateFormat("HH:mm:ss.SSS").format(new java.util.Date(System.currentTimeMillis())));
        return "EventController started";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<String> receiveMessage(
            @RequestBody Map<String, Object> body, @RequestHeader Map<String, String> headers) throws IOException, InterruptedException, ExecutionException {

        // Validate the number of available processors
        logger.info("EventController: Active processors: " + Runtime.getRuntime().availableProcessors());

        System.out.println("Header elements");
        for (String field : requiredFields) {
            if (headers.get(field) == null) {
                String msg = String.format("Missing expected header: %s.", field);
                System.out.println(msg);
                return new ResponseEntity<String>(msg, HttpStatus.BAD_REQUEST);
            } else {
                System.out.println(field + " : " + headers.get(field));
            }
        }

        System.out.println("Body elements");
        for (String bodyField : body.keySet()) {
            System.out.println(bodyField + " : " + body.get(bodyField));
        }

        if (headers.get("ce-subject") == null) {
            String msg = "Missing expected header: ce-subject.";
            System.out.println(msg);
            return new ResponseEntity<String>(msg, HttpStatus.BAD_REQUEST);
        }

        String ceSubject = headers.get("ce-subject");
        String msg = "Detected change in Cloud Storage bucket: (ce-subject) : " + ceSubject;
        System.out.println(msg);

        Map<String, String> message = (Map<String, String>) body.get("message");
        String quote = (String) message.get("quote");
        String author = (String) message.get("author");
        String book = (String) message.get("book");
        String randomID = (String) message.get("randomId");

        // Saving result to Firestore
        try {
            ApiFuture<WriteResult> writeResult = eventService.storeImage(quote, author, book, randomID);
            logger.info("Book metadata saved in Firestore at " + writeResult.get().getUpdateTime());
        } catch(IllegalArgumentException e){
            System.out.println("Could not write quote data to Firestore" + e.getMessage());
            return new ResponseEntity<String>(msg, HttpStatus.FAILED_DEPENDENCY);
        }

        return new ResponseEntity<String>(msg, HttpStatus.OK);
    }
}
// [END eventarc_audit_storage_handler]
