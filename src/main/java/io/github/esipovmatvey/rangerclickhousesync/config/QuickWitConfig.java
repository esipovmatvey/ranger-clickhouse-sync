package io.github.esipovmatvey.rangerclickhousesync.config;

import io.github.esipovmatvey.rangerclickhousesync.config.properties.QuickwitProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурация HTTP-клиента для взаимодействия с Quickwit REST API.
 * <p>
 * Определяет бин {@link RestTemplate} с базовым URL, указанным в свойствах {@code quickwit}.
 */
@Configuration
public class QuickWitConfig {

    @Bean(name = "quickwitRestTemplate")
    public RestTemplate quickWitRestTemplate(final QuickwitProperties properties) {
        return new RestTemplateBuilder().rootUri(properties.url())
                                        .build();
    }
}
