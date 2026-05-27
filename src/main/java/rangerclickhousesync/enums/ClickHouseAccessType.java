package rangerclickhousesync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rangerclickhousesync.engine.builder.grant.GrantStatementBuilder;

import java.util.Arrays;

/**
 * Enum для маппинга типов доступа Ranger на канонические имена привилегий ClickHouse.
 * <p>
 * Каждый элемент содержит имя, используемое в политиках Ranger ({@code rangerName}),
 * и соответствующее имя привилегии в синтаксисе {@code GRANT} ClickHouse ({@code clickhouseName}).
 * Применяется в {@link GrantStatementBuilder} для глобальных и системных привилегий.
 */
@Getter
@RequiredArgsConstructor
public enum ClickHouseAccessType {
    SHOW_USERS("show_users", "SHOW USERS"),
    CREATE_USER("create_user", "CREATE USER"),
    ALTER_USER("alter_user", "ALTER USER"),
    DROP_USER("drop_user", "DROP USER"),

    SHOW_ROLES("show_roles", "SHOW ROLES"),
    CREATE_ROLE("create_role", "CREATE ROLE"),
    DROP_ROLE("drop_role", "DROP ROLE"),

    CREATE_QUOTA("create_quota", "CREATE QUOTA"),
    DROP_QUOTA("drop_quota", "DROP QUOTA"),

    CREATE_SETTINGS_PROFILE("create_settings_profile", "CREATE SETTINGS PROFILE"),
    DROP_SETTINGS_PROFILE("drop_settings_profile", "DROP SETTINGS PROFILE"),

    ROLE_ADMIN("role_admin", "ROLE ADMIN"),
    ACCESS_MANAGEMENT("access_management", "ACCESS MANAGEMENT"),

    SYSTEM("system", "SYSTEM"),
    KILL_QUERY("kill_query", "KILL QUERY");

    private final String rangerName;
    private final String clickhouseName;

    public static ClickHouseAccessType from(final String rangerName) {
        return Arrays.stream(values())
                     .filter(v -> v.rangerName.equalsIgnoreCase(rangerName))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown access type: " + rangerName));
    }
}
