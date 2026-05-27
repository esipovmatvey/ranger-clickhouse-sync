package rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO для информации о маскировании данных в политике Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DataMaskInfoDto(
        String dataMaskType,
        String valueExpr
) {
}
