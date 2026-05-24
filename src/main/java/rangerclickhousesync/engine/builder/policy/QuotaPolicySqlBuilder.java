package rangerclickhousesync.engine.builder.policy;

import org.springframework.stereotype.Component;
import rangerclickhousesync.dto.AccessItemDto;
import rangerclickhousesync.dto.PolicyItemDto;
import rangerclickhousesync.dto.RangerPolicyDto;
import rangerclickhousesync.enums.RolePrefix;
import rangerclickhousesync.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Строит SQL-команды для политик квотирования (Quota).
 * <p>
 * Извлекает SQL-фрагмент квоты из условия политики с типом {@code quota_sql},
 * создаёт квоту в ClickHouse и привязывает её к роли, назначаемой пользователям.
 */
@Component
public class QuotaPolicySqlBuilder extends AbstractPolicySqlBuilder {
    private static final String QUOTA_SQL_CONDITION = "quota_sql";

    @Override
    public List<String> build(final RangerPolicyDto policy) {
        final List<String> sql = new ArrayList<>();
        final String quotaName = RolePrefix.QUOTA.getPrefix() + policy.id();

        policy.policyItems().forEach(item -> {
            final String quotaSql = extractQuotaSql(item);
            item.accesses().stream()
                .filter(AccessItemDto::isAllowed)
                .forEach(access -> {
                    final String roleName = RolePrefix.ROLE.getPrefix() + policy.id() + "_" + access.type();
                    final String grantSql = String.format("CREATE QUOTA IF NOT EXISTS %s %s TO %s", quotaName, quotaSql, roleName);
                    appendRoleWithGrants(sql, roleName, List.of(grantSql), item.users());
                });
        });
        return sql;
    }

    private String extractQuotaSql(final PolicyItemDto item) {
        return item.conditions()
                   .stream()
                   .filter(condition -> QUOTA_SQL_CONDITION.equals(condition.type()))
                   .flatMap(condition -> condition.values().stream())
                   .findFirst()
                   .map(StringUtils::unwrapQuotes)
                   .orElseThrow(() -> new IllegalArgumentException("Quota SQL condition is missing"));
    }
}
