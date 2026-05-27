package rangerclickhousesync.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Свойства подключения к ClickHouse.
 */
@ConfigurationProperties(prefix = "clickhouse")
public record ClickHouseProperties(
        String url,
        String username,
        String password
) {
}
