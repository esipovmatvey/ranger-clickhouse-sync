package rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO для элемента политики маскирования данных Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DataMaskPolicyItemDto(
        List<AccessItemDto> accesses,
        List<String> users,
        Boolean delegateAdmin,
        DataMaskInfoDto dataMaskInfo
) {
}
