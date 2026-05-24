package rangerclickhousesync.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Свойства подключения к Quickwit.
 */
@ConfigurationProperties(prefix = "quickwit")
public record QuickwitProperties(
        String url,
        String index,
        String indexEndpoint,
        String ingestEndpoint
) {
}
