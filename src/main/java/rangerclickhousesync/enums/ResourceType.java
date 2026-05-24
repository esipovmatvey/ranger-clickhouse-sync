package rangerclickhousesync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rangerclickhousesync.dto.ResourceDto;

import java.util.Arrays;
import java.util.Map;

/**
 * Тип ресурса на который распространяется политика Ranger.
 */
@Getter
@RequiredArgsConstructor
public enum ResourceType {
    QUOTA("quota"),
    COLUMN("column"),
    VIEW("view"),
    TABLE("table"),
    DATABASE("database"),
    ACCESS_MANAGEMENT("access_management"),
    GLOBAL("global");

    private final String resourceType;

    public static ResourceType fromResources(final Map<String, ResourceDto> resources) {
        return Arrays.stream(ResourceType.values())
                     .filter(type -> resources.containsKey(type.resourceType))
                     .findFirst()
                     .orElseThrow(() -> new RuntimeException("Unknown resource type"));
    }
}
