package io.github.esipovmatvey.rangerclickhousesync.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.esipovmatvey.rangerclickhousesync.config.properties.QuickwitProperties;
import io.github.esipovmatvey.rangerclickhousesync.dto.RangerAuditEventDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Клиент для взаимодействия с Quickwit через REST API.
 * <p>
 * Обеспечивает автоматическое создание индекса при старте приложения и
 * отправку событий аудита в формате NDJSON на ingest-эндпоинт Quickwit.
 */
@Component
@Slf4j
public class QuickwitClient {
    private final RestTemplate restTemplate;
    private final QuickwitProperties properties;
    private final String indexTemplate;

    public QuickwitClient(
            @Qualifier("quickwitRestTemplate") final RestTemplate restTemplate,
            final QuickwitProperties properties
    ) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.indexTemplate = loadIndexTemplate();
    }

    /**
     * Загружает шаблон конфигурации индекса Quickwit из ресурсов приложения.
     *
     * @return содержимое YAML-шаблона в виде строки
     * @throws RuntimeException если файл не удалось прочитать
     */
    private String loadIndexTemplate() {
        try {
            final ClassPathResource resource = new ClassPathResource("quickwit/quickwit_index_config.yaml");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Quickwit index template", e);
        }
    }

    /**
     * Проверяет существование индекса в Quickwit и создаёт его при необходимости.
     * Выполняется однократно после создания бина.
     */
    @PostConstruct
    public void ensureIndexExists() {
        final String indexName = properties.index();
        final String indexUrl = properties.indexEndpoint() + "/" + indexName;
        try {
            final ResponseEntity<String> response = restTemplate.getForEntity(indexUrl, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("QuickWit index '{}' already exists",indexName);
                return;
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.info("QuickWit index '{}' not found, creating...", indexName);
        }
        final String configYaml = String.format(indexTemplate, indexName);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/yaml"));
        final HttpEntity<String> request = new HttpEntity<>(configYaml, headers);
        restTemplate.postForEntity(properties.indexEndpoint(), request, String.class);
        log.info("Quickwit index '{}' created successfully", indexName);
    }

    /**
     * Отправляет список событий аудита в Quickwit.
     * События сериализуются в формат NDJSON и отправляются одним POST-запросом.
     *
     * @param events список событий для отправки.
     */
    public void ingestEvents(final List<RangerAuditEventDto> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final StringBuilder ndjson = new StringBuilder();
            for (final RangerAuditEventDto event : events) {
                ndjson.append(mapper.writeValueAsString(event)).append('\n');
            }
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            final HttpEntity<String> request = new HttpEntity<>(ndjson.toString(), headers);
            final String ingestUrl = properties.ingestEndpoint()
                                               .replace("{index}", properties.index()) + "?commit=force";

            restTemplate.postForEntity(ingestUrl, request, String.class);
        } catch (Exception e) {
            log.error("Failed to ingest audit events", e);
        }
    }
}
