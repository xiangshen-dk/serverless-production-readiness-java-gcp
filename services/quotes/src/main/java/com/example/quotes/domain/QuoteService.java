package com.example.quotes.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QuoteService {
  private final QuoteRepository quoteRepository;

  public QuoteService(QuoteRepository quoteRepository) {
    this.quoteRepository = quoteRepository;
  }

  public Quote findRandomQuote() {
    return quoteRepository.findRandomQuote();
  }

  public List<Quote> getAllQuotes() {
    return quoteRepository.findAll();
  }

  @Transactional
  public Quote createQuote(Quote quote){
    return quoteRepository.save(quote);
  }

  @Transactional
  public Quote updateQuote(Quote quote){
    return quoteRepository.save(quote);
  }

  public Optional<Quote> findById(Long id){
    return quoteRepository.findById(id);
  }

  public List<Quote> getByAuthor(String author) {
    return quoteRepository.findByAuthor(author);
  }

  public void deleteById(Long id){
    quoteRepository.deleteById(id);
  }
}
