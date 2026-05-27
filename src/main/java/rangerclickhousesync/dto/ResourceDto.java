package rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO для ресурса политики Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResourceDto(
        List<String> values,
        Boolean isExcludes,
        Boolean isRecursive
) {
}
