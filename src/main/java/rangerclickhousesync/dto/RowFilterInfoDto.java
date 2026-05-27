package rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO для информации о строковом фильтре в политике Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RowFilterInfoDto(String filterExpr) {
}
