package io.github.esipovmatvey.rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO для элемента политики доступа Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyItemDto(
        List<AccessItemDto> accesses,
        List<String> users,
        Boolean delegateAdmin
) {
}
