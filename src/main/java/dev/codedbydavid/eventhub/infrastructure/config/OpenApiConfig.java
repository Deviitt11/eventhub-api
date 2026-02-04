package dev.codedbydavid.eventhub.infrastructure.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Bean
    public OpenApiCustomizer correlationIdHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, item) -> {
                for (Operation op : item.readOperations()) {

                    boolean alreadyPresent = op.getParameters() != null
                            && op.getParameters().stream().anyMatch(p ->
                            "header".equalsIgnoreCase(p.getIn())
                                    && CORRELATION_HEADER.equalsIgnoreCase(p.getName())
                    );

                    if (alreadyPresent) {
                        continue;
                    }

                    Parameter correlationHeader = new Parameter()
                            .in("header")
                            .name(CORRELATION_HEADER)
                            .description("Request correlation identifier. If missing or invalid, the API generates one and returns it.")
                            .required(false)
                            .schema(new StringSchema().example("demo-123"));

                    op.addParametersItem(correlationHeader);
                }
            });
        };
    }
}