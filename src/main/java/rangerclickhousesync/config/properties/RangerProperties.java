package rangerclickhousesync.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Свойства подключения к Ranger.
 */
@ConfigurationProperties(prefix = "ranger")
public record RangerProperties(
        String url,
        String username,
        String password,
        String serviceName,
        String policiesEndpoint,
        Audit audit
) {
    public record Audit(int repoType) {
    }
}
