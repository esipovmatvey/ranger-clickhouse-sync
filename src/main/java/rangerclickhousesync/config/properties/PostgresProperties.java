package rangerclickhousesync.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Свойства подключения к PostgreSQL.
 */
@ConfigurationProperties(prefix = "postgres")
public record PostgresProperties(
        String url,
        String username,
        String password
) {
}
