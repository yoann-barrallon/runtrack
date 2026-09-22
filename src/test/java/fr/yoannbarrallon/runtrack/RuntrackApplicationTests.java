package fr.yoannbarrallon.runtrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=true")
@Testcontainers
@SuppressWarnings("resource")
class RuntrackApplicationTests {

	@Container
	static final PostgreSQLContainer postgres =
			new PostgreSQLContainer("postgres:16-alpine")
					.withDatabaseName("runtrack")
					.withUsername("runner")
					.withPassword("secret");

	@Container
	static final GenericContainer<?> redis =
			new GenericContainer<>("redis:7-alpine")
					.withExposedPorts(6379);

	@DynamicPropertySource
	static void configureInfrastructure(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", redis::getFirstMappedPort);
	}

	@Test
	void contextLoads() {
	}

}
