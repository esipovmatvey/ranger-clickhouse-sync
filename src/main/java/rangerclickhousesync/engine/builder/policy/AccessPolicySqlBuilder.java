package rangerclickhousesync.engine.builder.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rangerclickhousesync.dto.AccessItemDto;
import rangerclickhousesync.dto.RangerPolicyDto;
import rangerclickhousesync.dto.ResourceContext;
import rangerclickhousesync.engine.builder.grant.GrantStatementBuilder;
import rangerclickhousesync.enums.ResourceType;
import rangerclickhousesync.enums.RolePrefix;

import java.util.ArrayList;
import java.util.List;

/**
 * Строит SQL-команды для политик доступа (Access).
 * <p>
 * Генерирует создание роли, выдачу GRANT-ов на перечисленные в политике ресурсы
 * (таблицы, представления, колонки) и назначение роли пользователям.
 * Для каждого типа доступа (SELECT, INSERT, …) создаётся отдельная роль.
 */
@Component
@RequiredArgsConstructor
public class AccessPolicySqlBuilder extends AbstractPolicySqlBuilder {
    private final GrantStatementBuilder grantStatementBuilder;

    @Override
    public List<String> build(final RangerPolicyDto policy) {
        final List<String> sql = new ArrayList<>();
        final ResourceContext resource = ResourceContext.from(policy.resources());
        policy.policyItems()
              .forEach(item -> {
                  item.accesses().stream()
                      .filter(AccessItemDto::isAllowed)
                      .forEach(access -> {
                          final String roleName = RolePrefix.ROLE.getPrefix() + policy.id() + "_" + access.type();
                          final List<String> grantSqls = buildGrantSqls(access.type(), resource, roleName);
                          appendRoleWithGrants(sql, roleName, grantSqls, item.users());
                      });
              });
        return sql;
    }

    private List<String> buildGrantSqls(
            final String accessType,
            final ResourceContext resource,
            final String roleName
    ) {
        if (resource.resourceType() == ResourceType.COLUMN) {
            return List.of(grantStatementBuilder.build(accessType, resource, roleName));
        }
        final List<String> names = switch (resource.resourceType()) {
            case TABLE -> resource.tables();
            case VIEW -> resource.views();
            default -> List.of(resource.resourceName());
        };
        return names.stream()
                    .map(name -> grantStatementBuilder.build(accessType, singleResource(resource, name), roleName))
                    .toList();
    }

    private ResourceContext singleResource(final ResourceContext original, final String name) {
        return new ResourceContext(
                original.resourceType(),
                name,
                original.database(),
                original.resourceType() == ResourceType.TABLE ? List.of(name) : original.tables(),
                original.resourceType() == ResourceType.VIEW ? List.of(name) : original.views(),
                original.resourceType() == ResourceType.COLUMN ? original.columns() : List.of()
        );
    }
}
