package com.example.quotes;

import com.example.quotes.domain.QuoteRepository;
import com.example.quotes.domain.QuoteService;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import java.io.IOException;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
	@DisplayName("A random quote is returned")
	void testRandomQuotes() {
	  // var quote = this.quoteRepository.findRandomQuote();
	  var quote = this.quoteService.findRandomQuote();
	  assertThat(quote).isNotNull();
	}


}
