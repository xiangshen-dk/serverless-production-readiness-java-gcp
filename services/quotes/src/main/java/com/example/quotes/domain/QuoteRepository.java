package com.example.quotes.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuoteRepository extends JpaRepository<Quote,Long> {

    @Query( nativeQuery = true, value =
            "SELECT id,quote,author,book FROM quotes ORDER BY RANDOM() LIMIT 1")
    Quote findRandomQuote();

    Optional<Quote> findById(Long id);
    List<Quote> findByAuthor(String author);
}
