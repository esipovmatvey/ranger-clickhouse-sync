package rangerclickhousesync.engine.builder.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rangerclickhousesync.dto.RangerPolicyDto;
import rangerclickhousesync.dto.ResourceContext;
import rangerclickhousesync.engine.builder.grant.GrantStatementBuilder;
import rangerclickhousesync.enums.ResourceType;
import rangerclickhousesync.enums.RolePrefix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Строит SQL-команды для политик маскирования данных (Masking).
 * <p>
 * Создаёт одно представление, в котором указанные колонки заменяются
 * на выражения маскирования, переданные через {@code dataMaskInfo.valueExpr}.
 * Доступ к представлению предоставляется через роль, назначаемую пользователям.
 */
@Component
@RequiredArgsConstructor
public class MaskingPolicySqlBuilder extends AbstractPolicySqlBuilder {
    private final GrantStatementBuilder grantStatementBuilder;

    @Override
    public List<String> build(final RangerPolicyDto policy) {
        final ResourceContext resource = ResourceContext.from(policy.resources());
        final String database = resource.database();
        final String table = resource.tables().getFirst();
        final String viewName = RolePrefix.MASK_VIEW.getPrefix() + policy.id();
        final String roleName = RolePrefix.ROLE.getPrefix() + policy.id() + "_select";
        final List<String> maskedColumns = resource.columns();
        final String exceptPart = String.join(", ", maskedColumns);
        final String maskExpression = policy.dataMaskPolicyItems().stream()
                                            .map(item -> item.dataMaskInfo().valueExpr())
                                            .collect(Collectors.joining(", "));
        final String viewSql = String.format(
                "CREATE VIEW %s.%s DEFINER = default SQL SECURITY DEFINER AS SELECT * EXCEPT (%s), %s FROM %s.%s",
                database,
                viewName,
                exceptPart,
                maskExpression,
                database,
                table
        );
        final ResourceContext viewResource = new ResourceContext(
                ResourceType.VIEW,
                viewName,
                database,
                resource.tables(),
                resource.views(),
                resource.columns()
        );
        final String grantSql = grantStatementBuilder.build("select", viewResource, roleName);
        final List<String> sql = new ArrayList<>();
        sql.add(viewSql);
        appendRoleWithGrants(
                sql,
                roleName,
                List.of(grantSql),
                policy.dataMaskPolicyItems()
                      .stream()
                      .flatMap(i -> i.users().stream())
                      .distinct()
                      .toList()
        );
        return sql;
    }
}
