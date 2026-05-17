package io.github.esipovmatvey.rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * DTO для политики Apache Ranger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RangerPolicyDto(
        Long id,
        String guid,
        Boolean isEnabled,
        Integer version,
        String service,
        String name,
        Integer policyType,
        Integer policyPriority,
        Boolean isAuditEnabled,
        Map<String, ResourceDto> resources,
        List<PolicyItemDto> policyItems,
        List<RowFilterPolicyItemDto> rowFilterPolicyItems,
        List<DataMaskPolicyItemDto> dataMaskPolicyItems,
        String serviceType,
        Boolean isDenyAllElse
) {
}
