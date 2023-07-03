package com.example.audit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventService {

  private final FirestoreOptions firestoreOptions;
  private final Firestore firestore;

  public EventService() {
    this.firestoreOptions = FirestoreOptions.getDefaultInstance();
    this.firestore = firestoreOptions.getService();
  }
  public EventService(FirestoreOptions firestoreOptions, Firestore firestore) {
    this.firestoreOptions = firestoreOptions;
    this.firestore = firestore;
  }

  public ApiFuture<WriteResult> storeImage(String quote, String author, String book, String randomID) {
    DocumentReference doc = firestore.collection("books").document(author);

    Map<String, Object> data = new HashMap<>();
    data.put("created", new Date());

    return doc.set(data, SetOptions.merge());
  }

}
