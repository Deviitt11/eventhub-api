package dev.codedbydavid.eventhub.presentation.event;

import dev.codedbydavid.eventhub.EventHubApiApplication;
import dev.codedbydavid.eventhub.infrastructure.config.CorrelationIdFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.context.WebApplicationContext;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(classes = EventHubApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class EventControllerIT {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("eventhub")
            .withUsername("eventhub")
            .withPassword("eventhub");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Flyway must own schema creation in ITs (no Hibernate DDL)
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.clean-disabled", () -> "true");

        // Ensure Hibernate does NOT create/alter schema
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CorrelationIdFilter correlationIdFilter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(correlationIdFilter)
                .build();
    }

    @Test
    void crud_happy_path() throws Exception {
        Integer flywayTableExists = jdbcTemplate.queryForObject("""
	        select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'flyway_schema_history'
        """, Integer.class);

        assertThat(flywayTableExists).isEqualTo(1);

        Instant startsAt = Instant.now().plusSeconds(3600);
        Instant endsAt = startsAt.plusSeconds(3600);

        String createJson = """
		{
			"title": "My Event",
			"startsAt": "%s",
			"endsAt": "%s"
		}
		""".formatted(startsAt.toString(), endsAt.toString());

        String postBody = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("My Event"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(postBody);
        UUID id = UUID.fromString(created.get("id").asText());

        // Strong persistence proof: row exists in real Postgres (Testcontainers)
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from events where id = ?",
                Integer.class,
                id
        );
        assertThat(count).isEqualTo(1);

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("My Event"));

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Instant updatedEndsAt = endsAt.plusSeconds(3600);

        String updateJson = """
		{
			"title": "My Event Updated",
			"startsAt": "%s",
			"endsAt": "%s"
		}
		""".formatted(startsAt.toString(), updatedEndsAt.toString());

        mockMvc.perform(put("/api/v1/events/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("My Event Updated"));

        mockMvc.perform(delete("/api/v1/events/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/events/" + id));
    }

    @Test
    void validation_missing_title_returns_standard_error_payload() throws Exception {
        Instant startsAt = Instant.now().plusSeconds(3600);

        String json = """
		{
			"startsAt": "%s"
		}
		""".formatted(startsAt.toString());

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/events"));
    }

    @Test
    void not_found_returns_404_standard_payload() throws Exception {
        UUID randomId = UUID.randomUUID();

        String body = mockMvc.perform(get("/api/v1/events/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Event not found"))
                .andExpect(jsonPath("$.details").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/events/" + randomId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains(randomId.toString());
    }

    @Test
    void domain_validation_endsAt_not_after_startsAt_returns_400() throws Exception {
        Instant startsAt = Instant.now().plusSeconds(3600);

        String json = """
		{
			"title": "Bad Event",
			"startsAt": "%s",
			"endsAt": "%s"
		}
		""".formatted(startsAt.toString(), startsAt.toString());

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOMAIN_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Event validation failed"))
                .andExpect(jsonPath("$.details").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/events"));
    }

    @Test
    void correlation_id_is_added_when_missing() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(header().string("X-Correlation-Id", not(emptyOrNullString())));
    }

    @Test
    void correlation_id_is_echoed_when_provided() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                        .header("X-Correlation-Id", "demo-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "demo-123"));
    }

	/*
	1) Objective
	- Make ITs rely on Flyway migrations (not Hibernate DDL)
	- Prove persistence by querying real Postgres state after POST

	2) How
	- Remove ddl-auto=create-drop and set ddl-auto=validate
	- Force Flyway enabled + locations via DynamicPropertySource
	- Inject JdbcTemplate and assert row exists by id

	3) Why
	- Prevent “false green” tests where Hibernate creates schema
	- Ensure migrations are the source of truth across dev/IT/prod paths

	4) Checklist
	- Read: EventControllerIT overrideProps (Flyway + ddl-auto)
	- Test: ./gradlew integrationTest
	- Test: verify logs mention Flyway migrate on container DB
	- Test: CRUD test passes + DB count assertion == 1
	- Risk: if app requires a different schema/table name, SQL must match migration
	*/
}