package io.github.esipovmatvey.rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO для элемента политики строковой фильтрации Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RowFilterPolicyItemDto(
        List<AccessItemDto> accesses,
        List<String> users,
        Boolean delegateAdmin,
        RowFilterInfoDto rowFilterInfo
) {
}
