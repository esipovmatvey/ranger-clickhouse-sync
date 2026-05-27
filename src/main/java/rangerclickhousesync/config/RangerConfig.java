package rangerclickhousesync.config;

import rangerclickhousesync.config.properties.RangerProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Конфигурация HTTP-клиента для взаимодействия с REST API Apache Ranger.
 * <p>
 * Определяет бин {@link RestTemplate} с Basic-аутентификацией,
 * используя параметры из {@link RangerProperties}.
 */
@Configuration
public class RangerConfig {

    @Bean(name = "rangerRestTemplate")
    public RestTemplate rangerRestTemplate(final RangerProperties properties) {
        final String auth = properties.username() + ":" + properties.password();
        final String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        final ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
            request.getHeaders().add("Authorization", "Basic " + encodedAuth);
            return execution.execute(request, body);
        };

        return new RestTemplateBuilder().rootUri(properties.url())
                                        .additionalInterceptors(authInterceptor)
                                        .build();
    }
}
