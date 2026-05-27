package rangerclickhousesync.engine.builder.policy;

import org.springframework.stereotype.Component;
import rangerclickhousesync.dto.RangerPolicyDto;
import rangerclickhousesync.dto.ResourceContext;
import rangerclickhousesync.dto.RowFilterInfoDto;
import rangerclickhousesync.enums.RolePrefix;

import java.util.ArrayList;
import java.util.List;

/**
 * Строит SQL-команды для политик строковой фильтрации (Row Level Filter).
 * <p>
 * Создаёт в ClickHouse политику {@code CREATE ROW POLICY} с фильтрующим выражением,
 * ограничивающим видимость строк для указанных пользователей.
 */
@Component
public class RowPolicySqlBuilder implements PolicySqlBuilder {
    @Override
    public List<String> build(final RangerPolicyDto policy) {
        final List<String> sql = new ArrayList<>();
        final ResourceContext resource = ResourceContext.from(policy.resources());
        policy.rowFilterPolicyItems().forEach(item -> {
            final RowFilterInfoDto rowFilterInfo = item.rowFilterInfo();
            final String policyName = RolePrefix.ROW_POLICY.getPrefix() + policy.id();
            sql.add(String.format(
                    "CREATE ROW POLICY IF NOT EXISTS %s ON %s.%s FOR SELECT USING %s TO %s",
                    policyName,
                    resource.database(),
                    resource.tables().getFirst(),
                    rowFilterInfo.filterExpr(),
                    String.join(", ", item.users())
            ));
        });
        return sql;
    }
}
