package dev.codedbydavid.eventhub.presentation.event;
import dev.codedbydavid.eventhub.EventHubApiApplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(classes = EventHubApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class EventControllerIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("eventhub")
            .withUsername("eventhub")
            .withPassword("eventhub");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void crud_happy_path() throws Exception {
        Instant startsAt = Instant.parse("2026-01-27T10:00:00Z");
        Instant endsAt = Instant.parse("2026-01-27T11:00:00Z");

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

        mockMvc.perform(get("/api/v1/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("My Event"));

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        String updateJson = """
		{
			"title": "My Event Updated",
			"startsAt": "%s",
			"endsAt": "%s"
		}
		""".formatted(startsAt.toString(), Instant.parse("2026-01-27T12:00:00Z").toString());

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
        String json = """
		{
			"startsAt": "2026-01-27T10:00:00Z"
		}
		""";

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
        String json = """
		{
			"title": "Bad Event",
			"startsAt": "2026-01-27T10:00:00Z",
			"endsAt": "2026-01-27T10:00:00Z"
		}
		""";

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOMAIN_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Event validation failed"))
                .andExpect(jsonPath("$.details").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/events"));
    }
}