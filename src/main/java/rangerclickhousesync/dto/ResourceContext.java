package rangerclickhousesync.dto;

import rangerclickhousesync.enums.ResourceType;

import java.util.List;
import java.util.Map;

/**
 * Контекст ресурсов политики Ranger, извлечённый из мапы {@code resources}.
 * <p>
 * Содержит тип ресурса, имя базы данных, а также списки таблиц, представлений и колонок,
 * к которым применяется политика. Фабричный метод {@code from} собирает контекст
 * из сырого ответа Ranger Admin.
 */
public record ResourceContext(
        ResourceType resourceType,
        String resourceName,
        String database,
        List<String> tables,
        List<String> views,
        List<String> columns
) {
    public static ResourceContext from(final Map<String, ResourceDto> resources) {
        final ResourceType resourceType = ResourceType.fromResources(resources);
        return new ResourceContext(
                resourceType,
                first(resources, resourceType.getResourceType()),
                first(resources, ResourceType.DATABASE.getResourceType()),
                all(resources, ResourceType.TABLE.getResourceType()),
                all(resources, ResourceType.VIEW.getResourceType()),
                all(resources, ResourceType.COLUMN.getResourceType())
        );
    }

    private static String first(final Map<String, ResourceDto> resources, final String key) {
        final ResourceDto dto = resources.get(key);
        if (dto == null || dto.values() == null || dto.values().isEmpty()) {
            return null;
        }
        return dto.values().getFirst();
    }

    private static List<String> all(final Map<String, ResourceDto> resources, final String key) {
        final ResourceDto dto = resources.get(key);
        if (dto == null || dto.values() == null) {
            return List.of();
        }
        return List.copyOf(dto.values());
    }
}
