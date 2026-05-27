package rangerclickhousesync.client;

import rangerclickhousesync.config.properties.RangerProperties;
import rangerclickhousesync.dto.RangerPolicyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Клиент для взаимодействия с REST API Apache Ranger Admin.
 * <p>
 * Предоставляет методы для получения политик безопасности,
 * относящихся к заданному экземпляру сервиса.
 */
@Component
@RequiredArgsConstructor
public class RangerClient {
    private final RestTemplate rangerRestTemplate;
    private final RangerProperties rangerProperties;

    /**
     * Получает все политики для заданного экземпляра сервиса.
     *
     * @param serviceName имя экземпляра сервиса в Ranger
     * @return список политик {@link RangerPolicyDto} или {@code null}, если экземпляр сервиса не найден
     */
    public List<RangerPolicyDto> getPolicies(final String serviceName) {
        final ResponseEntity<List<RangerPolicyDto>> response = rangerRestTemplate.exchange(
                rangerProperties.policiesEndpoint(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {},
                serviceName
        );
        return response.getBody();
    }
}
