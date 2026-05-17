package io.github.esipovmatvey.rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO для элемента доступа в политике Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccessItemDto(
        String type,
        Boolean isAllowed
) {
}
