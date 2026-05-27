package rangerclickhousesync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Префиксы для объектов, создаваемых синхронизатором в ClickHouse.
 */
@Getter
@RequiredArgsConstructor
public enum RolePrefix {
    ROLE("ranger_role_"),
    MASK_VIEW("ranger_mask_"),
    ROW_POLICY("ranger_row_policy_"),
    QUOTA("ranger_quota_");

    private final String prefix;
}
