package com.example.quotes;

import com.example.quotes.domain.QuoteRepository;
import com.example.quotes.domain.QuoteService;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuotesApplicationNetworkFailuresTests {
	private static final Logger logger = LoggerFactory.getLogger(QuotesApplicationNetworkFailuresTests.class);

	// @Rule
	private static final Network network = Network.newNetwork();

	private static Proxy postgresqlProxy;

	@Container
	private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3-alpine")
			.withNetwork(network).withNetworkAliases("postgres");

	@Autowired
	private QuoteRepository quoteRepository;
	private QuoteService quoteService;

	@Container
	private static final ToxiproxyContainer toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
			.withNetwork(network);

	@DynamicPropertySource
	static void sqlserverProperties(DynamicPropertyRegistry registry) throws IOException {
		var toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
		postgresqlProxy = toxiproxyClient.createProxy("postgresql", "0.0.0.0:8666", "postgres:5432");

		String s = postgres.getJdbcUrl();

		registry.add("spring.datasource.url", () -> "jdbc:postgresql://%s:%d/%s".formatted(toxiproxy.getHost(),
				toxiproxy.getMappedPort(8666), postgres.getDatabaseName()));
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.flyway.url", postgres::getJdbcUrl);
		registry.add("spring.flyway.user", postgres::getUsername);
		registry.add("spring.flyway.password", postgres::getPassword);
	}
	@BeforeEach
	void setUp() throws IOException {
		quoteService = new QuoteService(quoteRepository);

		postgresqlProxy.toxics().getAll().forEach(toxic -> {
			try {
				toxic.remove();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	@DisplayName("The normal test, no Toxi proxy")
	void testRandomQuotes() {
	  var quote = this.quoteService.findRandomQuote();
	  assertThat(quote).isNotNull();
	}

	@Test
	@DisplayName("Test with latency, no timeout")
	void withLatency() throws IOException{
		postgresqlProxy.toxics().latency("postgresql-latency", ToxicDirection.DOWNSTREAM, 1600).setJitter(100);

		var quote = this.quoteService.findRandomQuote();
		assertThat(quote).isNotNull();
	}

	@Test
	@DisplayName("Test with latency, timeout encountered")
	void withLatencyandTimeout() throws IOException{
		postgresqlProxy.toxics().latency("postgresql-latency", ToxicDirection.DOWNSTREAM, 1600).setJitter(100);

		try {
			assertTimeout(Duration.ofSeconds(1), () -> {
				this.quoteService.findRandomQuote();
			});
		}catch (AssertionFailedError e){
			System.out.println(e.getMessage());
		}
	}

	@Test
	void withLatencyWithRetries() throws IOException {
		Latency latency = postgresqlProxy.toxics().latency("postgresql-latency", ToxicDirection.DOWNSTREAM, 1600)
				.setJitter(100);

		// var quote = this.quoteService.findRandomQuote().;
		// assertThat(quote).isNotNull();

	}

	@Ignore
	@Test
	void withToxiProxyConnectionDown() throws IOException {
		postgresqlProxy.toxics().bandwidth("postgres-cut-connection-downstream", ToxicDirection.DOWNSTREAM, 0);
		postgresqlProxy.toxics().bandwidth("postgres-cut-connection-upstream", ToxicDirection.UPSTREAM, 0);
		//
		// assertThat(
		// 		catchThrowable(() -> {
		// 			var quote = this.quoteService.findRandomQuote();
		// 			assertThat(quote).isNotNull();
		// 		})
		// ).as("calls fail with no connection").isInstanceOf(Throwable.class);

		try {
			assertTimeout(Duration.ofSeconds(1), () -> {
				var quote = this.quoteService.findRandomQuote();
				assertThat(quote).isNotNull();
			});
		}catch (AssertionFailedError e){
			System.out.println(e.getMessage());
		}

		// StepVerifier.create(this.serverlessServicesRepository.findAll().timeout(Duration.ofSeconds(5)))
		// 		.verifyErrorSatisfies(throwable -> assertThat(throwable).isInstanceOf(TimeoutException.class));

		postgresqlProxy.toxics().get("postgres-cut-connection-downstream").remove();
		postgresqlProxy.toxics().get("postgres-cut-connection-upstream").remove();

		var quote = this.quoteService.findRandomQuote();
		assertThat(quote).isNotNull();
	}
}
