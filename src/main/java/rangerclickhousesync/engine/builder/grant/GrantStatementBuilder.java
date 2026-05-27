package rangerclickhousesync.engine.builder.grant;

import org.springframework.stereotype.Component;
import rangerclickhousesync.dto.ResourceContext;
import rangerclickhousesync.enums.ClickHouseAccessType;
import rangerclickhousesync.enums.ResourceType;

/**
 * Формирует SQL-выражение {@code GRANT} для заданного ресурса и роли.
 * <p>
 * Поддерживает ресурсы: глобальные ({@code *.*}), колонки, базы данных, таблицы и представления.
 * Для глобальных привилегий и управления доступом использует канонические имена из {@link ClickHouseAccessType}.
 */
@Component
public class GrantStatementBuilder {

    public String build(
            final String accessType,
            final ResourceContext resource,
            final String roleName
    ) {
        final ResourceType resourceType = resource.resourceType();
        if (resourceType == ResourceType.GLOBAL || resourceType == ResourceType.ACCESS_MANAGEMENT) {
            final ClickHouseAccessType type = ClickHouseAccessType.from(accessType);
            return String.format("GRANT %s ON *.* TO %s", type.getClickhouseName(), roleName);
        }
        if (resourceType == ResourceType.COLUMN) {
            return buildColumnGrant(accessType, resource, roleName);
        }
        return String.format(
                "GRANT %s ON %s TO %s",
                accessType.toUpperCase(),
                resolveResource(resource),
                roleName
        );
    }

    private String buildColumnGrant(
            final String accessType,
            final ResourceContext resource,
            final String roleName
    ) {
        return String.format(
                "GRANT %s(%s) ON %s.%s TO %s",
                accessType.toUpperCase(),
                String.join(", ", resource.columns()),
                resource.database(),
                resource.tables().getFirst(),
                roleName
        );
    }

    private String resolveResource(final ResourceContext resource) {
        return switch (resource.resourceType()) {
            case DATABASE -> resolveDatabase(resource);
            case TABLE -> resolveTable(resource);
            case VIEW -> resolveView(resource);
            default -> "*.*";
        };
    }

    private String resolveDatabase(final ResourceContext resource) {
        if (resource.resourceName() == null || "*".equals(resource.resourceName())) {
            return "*.*";
        }
        return resource.resourceName() + ".*";
    }

    private String resolveTable(final ResourceContext resource) {
        if (resource.database() == null) {
            return "*.*";
        }
        if (resource.resourceName() == null || "*".equals(resource.resourceName())) {
            return resource.database() + ".*";
        }
        return resource.database() + "." + resource.resourceName();
    }

    private String resolveView(final ResourceContext resource) {
        if (resource.resourceName() == null) {
            return "*.*";
        }
        return resource.database() + "." + resource.resourceName();
    }
}
