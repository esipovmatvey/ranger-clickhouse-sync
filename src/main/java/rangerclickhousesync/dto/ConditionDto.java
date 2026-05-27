package rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO для условия политики Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConditionDto(
        String type,
        List<String> values
) {
}
